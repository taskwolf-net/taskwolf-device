package net.taskwolf.device;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.distribution.packet.PacketRegistry;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.Module;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.core.trigger.TriggerFactory;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.device.action.DeviceActionFactory;
import net.taskwolf.device.action.DeviceCommandAction;
import net.taskwolf.device.action.DeviceNotificationAction;
import net.taskwolf.device.command.CommandFactory;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.connection.DeviceWebSocket;
import net.taskwolf.device.distribution.command.event.CommandRequestEvent;
import net.taskwolf.device.distribution.command.event.CommandResponseEvent;
import net.taskwolf.device.distribution.command.hook.CommandRequestHook;
import net.taskwolf.device.distribution.command.hook.CommandResponseHook;
import net.taskwolf.device.distribution.device.event.DeviceLoginEvent;
import net.taskwolf.device.distribution.device.event.DeviceLogoutEvent;
import net.taskwolf.device.distribution.device.packet.incoming.PacketIncomingDeviceLogin;
import net.taskwolf.device.distribution.device.packet.incoming.PacketIncomingDeviceLogout;
import net.taskwolf.device.distribution.file.event.FileInfoRequestEvent;
import net.taskwolf.device.distribution.file.event.FileInfoResponseEvent;
import net.taskwolf.device.distribution.file.event.FileStorageRequestEvent;
import net.taskwolf.device.distribution.file.event.FileStorageResponseEvent;
import net.taskwolf.device.distribution.file.hook.FileInfoRequestHook;
import net.taskwolf.device.distribution.file.hook.FileInfoResponseHook;
import net.taskwolf.device.distribution.file.hook.FileStorageRequestHook;
import net.taskwolf.device.distribution.file.hook.FileStorageResponseHook;
import net.taskwolf.device.distribution.notification.event.NotificationRequestEvent;
import net.taskwolf.device.distribution.notification.event.NotificationResponseEvent;
import net.taskwolf.device.distribution.notification.hook.NotificationRequestHook;
import net.taskwolf.device.distribution.notification.hook.NotificationResponseHook;
import net.taskwolf.device.distribution.command.packet.incoming.PacketIncomingCommandRequest;
import net.taskwolf.device.distribution.command.packet.incoming.PacketIncomingCommandResponse;
import net.taskwolf.device.distribution.file.packet.incoming.PacketIncomingFileInfoRequest;
import net.taskwolf.device.distribution.file.packet.incoming.PacketIncomingFileInfoResponse;
import net.taskwolf.device.distribution.file.packet.incoming.PacketIncomingFileStorageRequest;
import net.taskwolf.device.distribution.file.packet.incoming.PacketIncomingFileStorageResponse;
import net.taskwolf.device.distribution.notification.packet.incoming.PacketIncomingNotificationRequest;
import net.taskwolf.device.distribution.notification.packet.incoming.PacketIncomingNotificationResponse;
import net.taskwolf.device.distribution.device.packet.outgoing.PacketOutgoingDeviceLogout;
import net.taskwolf.device.notification.NotificationFactory;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import net.taskwolf.device.trigger.DeviceTriggerFactory;
import org.springframework.boot.SpringApplication;

import java.security.Key;
import java.util.List;

@ModuleDescription(name = "device", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class DeviceModule extends Module {
  private Log log;
  private TriggerFactory triggerFactory;
  private ActionFactory actionFactory;
  private AccountLink accountLink;
  private InputComponentSelect deviceComponentSelect;
  private DeviceWebSocket socket;

  public DeviceModule(Injector injector) {
    super(injector.createChildInjector(DeviceInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Device");
    injector().getInstance(SpringApplication.class).addInitializers(
      injector().getInstance(DeviceContextInitializer.class));
    triggerFactory = DeviceTriggerFactory.create();
    var deviceDatabaseTable = injector().getInstance(DeviceDatabaseTable.class);
    var clientRegistry = injector().getInstance(DistributionClientRegistry.class);
    actionFactory = DeviceActionFactory.create(deviceDatabaseTable,
      injector().getInstance(NotificationFactory.class),
      injector().getInstance(CommandFactory.class));
    accountLink = DeviceAccountLink.create();
    deviceComponentSelect = DeviceComponentSelect.create(deviceDatabaseTable,
      injector().getInstance(UserDeviceDatabaseTable.class));
    registerPackets();
    registerPacketEvents();
    registerHooks();
    socket = DeviceWebSocket.of(
      injector().getInstance(DeviceConfiguration.class).webSocketPort(),
      deviceDatabaseTable, injector().getInstance(DeviceConnectionRepository.class),
      clientRegistry, injector().getInstance(Key.class));
    socket.start();
  }

  private void registerPackets() throws Exception {
    var packetRegistry = injector().getInstance(PacketRegistry.class);
    packetRegistry.registerPacket(PacketIncomingDeviceLogin.class);
    packetRegistry.registerPacket(PacketIncomingDeviceLogout.class);
    packetRegistry.registerPacket(PacketIncomingNotificationRequest.class);
    packetRegistry.registerPacket(PacketIncomingNotificationResponse.class);
    packetRegistry.registerPacket(PacketIncomingCommandRequest.class);
    packetRegistry.registerPacket(PacketIncomingCommandResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileStorageRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileStorageResponse.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoRequest.class);
    packetRegistry.registerPacket(PacketIncomingFileInfoResponse.class);
  }

  private void registerPacketEvents() {
    var packetEventRepository = injector().getInstance(PacketEventRepository.class);
    packetEventRepository.registerEvent(PacketIncomingDeviceLogin.class,
      (client, packet) -> DeviceLoginEvent.create(packet.deviceId(), client));
    packetEventRepository.registerEvent(PacketIncomingDeviceLogout.class,
      (client, packet) -> DeviceLogoutEvent.create(packet.deviceId()));
    registerNotificationPacketEvents(packetEventRepository);
    registerCommandPacketEvents(packetEventRepository);
    registerFilePacketEvents(packetEventRepository);
  }

  private void registerNotificationPacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingNotificationRequest.class,
      (client, packet) -> NotificationRequestEvent.create(packet.notificationId(),
        packet.deviceId(), packet.title(), packet.body(), client));
    repository.registerEvent(PacketIncomingNotificationResponse.class,
      (client, packet) -> NotificationResponseEvent.create(packet.notificationId(),
        packet.delivered()));
  }

  private void registerCommandPacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingCommandRequest.class,
      (client, packet) -> CommandRequestEvent.create(packet.commandId(),
        packet.deviceId(), packet.command(), client));
    repository.registerEvent(PacketIncomingCommandResponse.class,
      (client, packet) -> CommandResponseEvent.create(packet.commandId(),
        packet.delivered(), packet.output(), packet.errorMessage(),
        packet.exitCode()));
  }

  private void registerFilePacketEvents(PacketEventRepository repository) {
    repository.registerEvent(PacketIncomingFileStorageRequest.class,
      (client, packet) -> FileStorageRequestEvent.create(packet.storageId(),
        packet.deviceId(), packet.path(), packet.content(), client));
    repository.registerEvent(PacketIncomingFileStorageResponse.class,
      (client, packet) -> FileStorageResponseEvent.create(packet.storageId(),
        packet.success()));
    repository.registerEvent(PacketIncomingFileInfoRequest.class,
      (client, packet) -> FileInfoRequestEvent.create(packet.infoId(),
        packet.deviceId(), packet.path(), client));
    repository.registerEvent(PacketIncomingFileInfoResponse.class,
      (client, packet) -> FileInfoResponseEvent.create(packet.infoId(),
        packet.content(), packet.success()));
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
  }

  @Override
  public void disable() throws Exception {
    var connections = injector().getInstance(DeviceConnectionRepository.class)
      .allConnection();
    var proxy = injector().getInstance(DistributionClientRegistry.class)
      .findClientsByType(NodeType.PROXY).stream().findFirst().get();
    for (var connection : connections) {
      proxy.sendPacket(new PacketOutgoingDeviceLogout(connection.device().id()));
      connection.close();
    }
    socket.stop();
  }

  @Override
  public TriggerFactory triggerFactory() {
    return triggerFactory;
  }

  @Override
  public ActionFactory actionFactory() {
    return actionFactory;
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
  public List<TriggerInformation> triggerInformation() {
    return Lists.newArrayList();
  }

  @Override
  public List<ActionInformation> actionInformation() {
    return Lists.newArrayList(
      DeviceNotificationAction.information(deviceComponentSelect),
      DeviceCommandAction.information(deviceComponentSelect));
  }
}