package com.dulno.device;

import com.dulno.device.action.file.delete.DeviceFileDeleteAction;
import com.dulno.device.action.file.info.DeviceFileInfoAction;
import com.dulno.device.action.folder.delete.DeviceFolderDeleteAction;
import com.dulno.device.distribution.file.event.*;
import com.dulno.device.distribution.file.hook.*;
import com.dulno.device.distribution.file.packet.incoming.*;
import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.dulno.device.structure.UserDeviceDatabaseTable;
import com.dulno.device.trigger.notification.DeviceNotificationTrigger;
import com.dulno.workflow.integration.Integration;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.dulno.core.account.AccountLink;
import com.dulno.workflow.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.event.HookRegistry;
import com.dulno.core.log.Log;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.workflow.trigger.TriggerRepository;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.device.action.command.DeviceCommandAction;
import com.dulno.device.action.file.store.DeviceFileStoreAction;
import com.dulno.device.action.folder.create.DeviceFolderCreateAction;
import com.dulno.device.action.notification.DeviceNotificationAction;
import com.dulno.device.command.CommandFactory;
import com.dulno.device.connection.DeviceConnectionRepository;
import com.dulno.device.connection.DeviceWebSocket;
import com.dulno.device.distribution.command.event.WorkerCommandRequestEvent;
import com.dulno.device.distribution.command.event.WorkerCommandResponseEvent;
import com.dulno.device.distribution.command.hook.CommandRequestHook;
import com.dulno.device.distribution.command.hook.CommandResponseHook;
import com.dulno.device.distribution.command.packet.incoming.PacketIncomingCommandRequest;
import com.dulno.device.distribution.command.packet.incoming.PacketIncomingCommandResponse;
import com.dulno.device.distribution.device.packet.outgoing.PacketOutgoingDeviceLogout;
import com.dulno.device.distribution.notification.event.WorkerNotificationRequestEvent;
import com.dulno.device.distribution.notification.event.WorkerNotificationResponseEvent;
import com.dulno.device.distribution.notification.hook.NotificationRequestHook;
import com.dulno.device.distribution.notification.hook.NotificationResponseHook;
import com.dulno.device.distribution.notification.packet.incoming.PacketIncomingNotificationRequest;
import com.dulno.device.distribution.notification.packet.incoming.PacketIncomingNotificationResponse;
import com.dulno.device.file.FileFactory;
import com.dulno.device.file.workspace.FileWorkspaceComponentSelect;
import com.dulno.device.notification.NotificationFactory;
import com.dulno.device.trigger.command.DeviceCommandTrigger;
import com.dulno.device.trigger.file.create.DeviceFileCreateTrigger;
import com.dulno.device.trigger.file.delete.DeviceFileDeleteTrigger;
import com.dulno.device.trigger.folder.create.DeviceFolderCreateTrigger;
import com.dulno.device.trigger.folder.delete.DeviceFolderDeleteTrigger;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "device", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DeviceModule extends Integration {
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
    var userDeviceDatabaseTable = injector().getInstance(UserDeviceDatabaseTable.class);
    accountLink = DeviceAccountLink.create();
    deviceComponentSelect = DeviceComponentSelect.create(userDeviceDatabaseTable);
    fileWorkspaceComponentSelect = FileWorkspaceComponentSelect.create(
      injector().getInstance(FileWorkspaceDatabaseTable.class),
      userDeviceDatabaseTable);
    registerPackets();
    registerPacketEvents();
    registerHooks();
    socket = DeviceWebSocket.of(
      injector().getInstance(DeviceConfiguration.class).webSocketPort(),
      deviceDatabaseTable, injector().getInstance(DeviceConnectionRepository.class),
      injector().getInstance(WorkerProxyClient.class),
      injector().getInstance(com.google.inject.Key.get(java.security.Key.class,
        Names.named("productKey"))));
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
    packetRegistry.registerPacket(PacketIncomingFileStorageRedirectResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoRedirectResponse.class);
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
        packet.deviceId(), packet.devicePlatform(), packet.command()));
    repository.registerEvent(PacketIncomingCommandResponse.class,
      (client, packet) -> WorkerCommandResponseEvent.create(packet.commandId(),
        packet.delivered(), packet.output(), packet.errorMessage(),
        packet.exitCode()));
  }

  private void registerFilePacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingFileStorageRequest.class,
      (client, packet) -> WorkerFileStorageRequestEvent.create(packet.storageId(),
        packet.deviceId(), packet.devicePlatform(), packet.filePath(),
        packet.fileName()));
    repository.registerEvent(PacketIncomingFileStorageResponse.class,
      (client, packet) -> WorkerFileStorageResponseEvent.create(packet.storageId(),
        packet.success()));
    repository.registerEvent(PacketIncomingFileStorageRedirectResponse.class,
      (client, packet) -> WorkerFileStorageRedirectResponseEvent.create(
        packet.storageId(), packet.redirectUrl()));
    repository.registerEvent(PacketIncomingFileInfoRequest.class,
      (client, packet) -> WorkerFileInfoRequestEvent.create(packet.infoId(),
        packet.deviceId(), packet.devicePlatform(), packet.filePath(),
        packet.fileName()));
    repository.registerEvent(PacketIncomingFileInfoResponse.class,
      (client, packet) -> WorkerFileInfoResponseEvent.create(packet.infoId(),
        packet.success()));
    repository.registerEvent(PacketIncomingFileInfoRedirectResponse.class,
      (client, packet) -> WorkerFileInfoRedirectResponseEvent.create(
        packet.infoId(), packet.redirectUrl()));
    repository.registerEvent(PacketIncomingFileDeleteRequest.class,
      (client, packet) -> WorkerFileDeleteRequestEvent.create(packet.deleteId(),
        packet.deviceId(), packet.devicePlatform(), packet.filePath(),
        packet.fileName()));
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
    hookRegistry.register(injector().getInstance(FileStorageRedirectResponseHook.class));
    hookRegistry.register(injector().getInstance(FileInfoRequestHook.class));
    hookRegistry.register(injector().getInstance(FileInfoResponseHook.class));
    hookRegistry.register(injector().getInstance(FileInfoRedirectResponseHook.class));
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
    return ModuleInformation.create("device.module", "", "device.png",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public TriggerRepository triggerRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var deviceDatabaseTable = injector().getInstance(UserDeviceDatabaseTable.class);
    var repository = TriggerRepository.create();
    repository.registerTrigger(DeviceNotificationTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceCommandTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, databaseConnection, databaseKeyspace));
    repository.registerTrigger(DeviceFileCreateTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, fileWorkspaceComponentSelect, databaseConnection,
      databaseKeyspace));
    repository.registerTrigger(DeviceFileDeleteTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, fileWorkspaceComponentSelect, databaseConnection,
      databaseKeyspace));
    repository.registerTrigger(DeviceFolderCreateTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, fileWorkspaceComponentSelect, databaseConnection,
      databaseKeyspace));
    repository.registerTrigger(DeviceFolderDeleteTrigger.create(deviceDatabaseTable,
      deviceComponentSelect, fileWorkspaceComponentSelect, databaseConnection,
      databaseKeyspace));
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var deviceDatabaseTable = injector().getInstance(DeviceDatabaseTable.class);
    var userDeviceDatabaseTable = injector().getInstance(UserDeviceDatabaseTable.class);
    var workspaceDatabaseTable = injector().getInstance(FileWorkspaceDatabaseTable.class);
    var fileFactory = injector().getInstance(FileFactory.class);
    var repository = ActionRepository.create();
    repository.registerAction(DeviceNotificationAction.create(deviceComponentSelect,
      deviceDatabaseTable, userDeviceDatabaseTable,
      injector().getInstance(NotificationFactory.class), databaseConnection,
      databaseKeyspace));
    repository.registerAction(DeviceCommandAction.create(deviceComponentSelect,
      deviceDatabaseTable, userDeviceDatabaseTable,
      injector().getInstance(CommandFactory.class), databaseConnection,
      databaseKeyspace));
    repository.registerAction(DeviceFileStoreAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFileInfoAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFileDeleteAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFolderCreateAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory, databaseConnection, databaseKeyspace));
    repository.registerAction(DeviceFolderDeleteAction.create(deviceComponentSelect,
      fileWorkspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory, databaseConnection, databaseKeyspace));
    return repository;
  }
}