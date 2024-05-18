package net.taskwolf.device;

import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.trigger.TriggerRepository;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.device.action.command.DeviceCommandAction;
import net.taskwolf.device.action.file.delete.DeviceFileDeleteAction;
import net.taskwolf.device.action.file.info.DeviceFileInfoAction;
import net.taskwolf.device.action.file.store.DeviceFileStoreAction;
import net.taskwolf.device.action.folder.create.DeviceFolderCreateAction;
import net.taskwolf.device.action.folder.delete.DeviceFolderDeleteAction;
import net.taskwolf.device.action.notification.DeviceNotificationAction;
import net.taskwolf.device.command.CommandFactory;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.connection.DeviceWebSocket;
import net.taskwolf.device.distribution.command.event.WorkerCommandRequestEvent;
import net.taskwolf.device.distribution.command.event.WorkerCommandResponseEvent;
import net.taskwolf.device.distribution.command.hook.CommandRequestHook;
import net.taskwolf.device.distribution.command.hook.CommandResponseHook;
import net.taskwolf.device.distribution.command.packet.incoming.PacketIncomingCommandRequest;
import net.taskwolf.device.distribution.command.packet.incoming.PacketIncomingCommandResponse;
import net.taskwolf.device.distribution.device.packet.outgoing.PacketOutgoingDeviceLogout;
import net.taskwolf.device.distribution.file.event.*;
import net.taskwolf.device.distribution.file.hook.*;
import net.taskwolf.device.distribution.file.packet.incoming.*;
import net.taskwolf.device.distribution.notification.event.WorkerNotificationRequestEvent;
import net.taskwolf.device.distribution.notification.event.WorkerNotificationResponseEvent;
import net.taskwolf.device.distribution.notification.hook.NotificationRequestHook;
import net.taskwolf.device.distribution.notification.hook.NotificationResponseHook;
import net.taskwolf.device.distribution.notification.packet.incoming.PacketIncomingNotificationRequest;
import net.taskwolf.device.distribution.notification.packet.incoming.PacketIncomingNotificationResponse;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspaceComponentSelect;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import net.taskwolf.device.trigger.command.DeviceCommandTrigger;
import net.taskwolf.device.trigger.file.create.DeviceFileCreateTrigger;
import net.taskwolf.device.trigger.file.delete.DeviceFileDeleteTrigger;
import net.taskwolf.device.trigger.folder.create.DeviceFolderCreateTrigger;
import net.taskwolf.device.trigger.folder.delete.DeviceFolderDeleteTrigger;
import net.taskwolf.device.trigger.notification.DeviceNotificationTrigger;
import org.springframework.boot.SpringApplication;

import java.security.Key;

@ModuleDescription(name = "device", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DeviceModule extends Module {
  private Log log;
  private AccountLink accountLink;
  private InputComponentSelect deviceComponentSelect;
  private InputComponentSelect fileWorkspaceComponentSelect;
  private DeviceWebSocket socket;

  public DeviceModule(Injector injector) {
    super(injector.createChildInjector(DeviceInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Device");
    injector().getInstance(SpringApplication.class).addInitializers(
      injector().getInstance(DeviceContextInitializer.class));
    var deviceDatabaseTable = injector().getInstance(DeviceDatabaseTable.class);
    accountLink = DeviceAccountLink.create();
    deviceComponentSelect = DeviceComponentSelect.create(deviceDatabaseTable,
      injector().getInstance(UserDeviceDatabaseTable.class));
    fileWorkspaceComponentSelect = FileWorkspaceComponentSelect.create(
      injector().getInstance(FileWorkspaceDatabaseTable.class));
    registerPackets();
    registerPacketEvents();
    registerHooks();
    socket = DeviceWebSocket.of(
      injector().getInstance(DeviceConfiguration.class).webSocketPort(),
      deviceDatabaseTable, injector().getInstance(DeviceConnectionRepository.class),
      injector().getInstance(WorkerProxyClient.class), injector().getInstance(Key.class));
    socket.start();
  }

  private void registerPackets() throws Exception {
    var packetRegistry = injector().getInstance(PacketRegistry.class);
    packetRegistry.registerPacket(PacketIncomingNotificationRequest.class);
    packetRegistry.registerPacket(PacketIncomingNotificationResponse.class);
    packetRegistry.registerPacket(PacketIncomingCommandRequest.class);
    packetRegistry.registerPacket(PacketIncomingCommandResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileStorageRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileStorageResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileDeleteRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileDeleteResponse.class);
  }

  private void registerPacketEvents() {
    var packetEventRepository = injector().getInstance(PacketEventRepository.class);
    registerNotificationPacketEvents(packetEventRepository);
    registerCommandPacketEvents(packetEventRepository);
    registerFilePacketEvents(packetEventRepository);
  }

  private void registerNotificationPacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingNotificationRequest.class,
      (client, packet) -> WorkerNotificationRequestEvent.create(packet.notificationId(),
        packet.deviceId(), packet.title(), packet.body()));
    repository.registerEvent(PacketIncomingNotificationResponse.class,
      (client, packet) -> WorkerNotificationResponseEvent.create(packet.notificationId(),
        packet.delivered()));
  }

  private void registerCommandPacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingCommandRequest.class,
      (client, packet) -> WorkerCommandRequestEvent.create(packet.commandId(),
        packet.deviceId(), packet.command()));
    repository.registerEvent(PacketIncomingCommandResponse.class,
      (client, packet) -> WorkerCommandResponseEvent.create(packet.commandId(),
        packet.delivered(), packet.output(), packet.errorMessage(),
        packet.exitCode()));
  }

  private void registerFilePacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingFileStorageRequest.class,
      (client, packet) -> WorkerFileStorageRequestEvent.create(packet.storageId(),
        packet.deviceId(), packet.filePath(), packet.fileName(),
        packet.content()));
    repository.registerEvent(PacketIncomingFileStorageResponse.class,
      (client, packet) -> WorkerFileStorageResponseEvent.create(packet.storageId(),
        packet.success()));
    repository.registerEvent(PacketIncomingFileInfoRequest.class,
      (client, packet) -> WorkerFileInfoRequestEvent.create(packet.infoId(),
        packet.deviceId(), packet.filePath(), packet.fileName()));
    repository.registerEvent(PacketIncomingFileInfoResponse.class,
      (client, packet) -> WorkerFileInfoResponseEvent.create(packet.infoId(),
        packet.content(), packet.success()));
    repository.registerEvent(PacketIncomingFileDeleteRequest.class,
      (client, packet) -> WorkerFileDeleteRequestEvent.create(packet.deleteId(),
        packet.deviceId(), packet.filePath(), packet.fileName()));
    repository.registerEvent(PacketIncomingFileDeleteResponse.class,
      (client, packet) -> WorkerFileDeleteResponseEvent.create(packet.deleteId(),
        packet.success()));
  }

  private void registerHooks() {
    var hookRegistry = injector().getInstance(HookRegistry.class);
    hookRegistry.register(injector().getInstance(CommandRequestHook.class));
    hookRegistry.register(injector().getInstance(CommandResponseHook.class));
    hookRegistry.register(injector().getInstance(NotificationRequestHook.class));
    hookRegistry.register(injector().getInstance(NotificationResponseHook.class));
    hookRegistry.register(injector().getInstance(FileStorageRequestHook.class));
    hookRegistry.register(injector().getInstance(FileStorageResponseHook.class));
    hookRegistry.register(injector().getInstance(FileInfoRequestHook.class));
    hookRegistry.register(injector().getInstance(FileInfoResponseHook.class));
    hookRegistry.register(injector().getInstance(FileDeleteRequestHook.class));
    hookRegistry.register(injector().getInstance(FileDeleteResponseHook.class));
  }

  @Override
  public void disable() throws Exception {
    var connections = injector().getInstance(DeviceConnectionRepository.class)
      .allConnection();
    var proxy = injector().getInstance(WorkerProxyClient.class);
    for (var connection : connections) {
      proxy.sendPacket(new PacketOutgoingDeviceLogout(connection.device().id()));
      connection.close();
    }
    socket.stop();
  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Device", "", "device.png",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public TriggerRepository triggerRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = TriggerRepository.create();
    repository.registerTrigger(DeviceNotificationTrigger.create(deviceComponentSelect,
      databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceCommandTrigger.create(deviceComponentSelect,
      databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceFileCreateTrigger.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceFileDeleteTrigger.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceFolderCreateTrigger.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceFolderDeleteTrigger.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, databaseConnection, databaseKeyspace));
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var deviceDatabaseTable = injector().getInstance(DeviceDatabaseTable.class);
    var workspaceDatabaseTable = injector().getInstance(FileWorkspaceDatabaseTable.class);
    var fileFactory = injector().getInstance(FileFactory.class);
    var repository = ActionRepository.create();
    repository.registerAction(DeviceNotificationAction.create(deviceComponentSelect,
      deviceDatabaseTable, injector().getInstance(NotificationFactory.class),
      databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceCommandAction.create(deviceComponentSelect,
      deviceDatabaseTable, injector().getInstance(CommandFactory.class),
      databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFileStoreAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, workspaceDatabaseTable,
      fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFileInfoAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, workspaceDatabaseTable,
      fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFileDeleteAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, workspaceDatabaseTable,
      fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFolderCreateAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, workspaceDatabaseTable,
      fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFolderDeleteAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, workspaceDatabaseTable,
      fileFactory, databaseConnection, databaseKeyspace));
    return repository;
  }
}