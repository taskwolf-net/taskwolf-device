package net.taskwolf.device;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.action.ActionFactory;
import net.taskwolf.core.action.ActionInformation;
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
import net.taskwolf.device.distribution.event.*;
import net.taskwolf.device.distribution.hook.CommandRequestHook;
import net.taskwolf.device.distribution.hook.CommandResponseHook;
import net.taskwolf.device.distribution.hook.NotificationRequestHook;
import net.taskwolf.device.distribution.hook.NotificationResponseHook;
import net.taskwolf.device.distribution.packet.incoming.*;
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
    var socket = DeviceWebSocket.of(5151, deviceDatabaseTable,
      injector().getInstance(DeviceConnectionRepository.class), clientRegistry,
      injector().getInstance(Key.class));
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
  }

  private void registerPacketEvents() {
    var packetEventRepository = injector().getInstance(PacketEventRepository.class);
    packetEventRepository.registerEvent(PacketIncomingDeviceLogin.class,
      (client, packet) -> DeviceLoginEvent.create(packet.deviceId(), client));
    packetEventRepository.registerEvent(PacketIncomingDeviceLogout.class,
      (client, packet) -> DeviceLogoutEvent.create(packet.deviceId()));
    packetEventRepository.registerEvent(PacketIncomingNotificationRequest.class,
      (client, packet) -> NotificationRequestEvent.create(packet.notificationId(),
        packet.deviceId(), packet.title(), packet.body(), client));
    packetEventRepository.registerEvent(PacketIncomingNotificationResponse.class,
      (client, packet) -> NotificationResponseEvent.create(packet.notificationId(),
        packet.delivered()));
    packetEventRepository.registerEvent(PacketIncomingCommandRequest.class,
      (client, packet) -> CommandRequestEvent.create(packet.commandId(),
        packet.deviceId(), packet.command(), client));
    packetEventRepository.registerEvent(PacketIncomingCommandResponse.class,
      (client, packet) -> CommandResponseEvent.create(packet.commandId(),
        packet.delivered(), packet.output(), packet.errorMessage(),
        packet.exitCode()));
  }

  private void registerHooks() {
    var hookRegistry = injector().getInstance(HookRegistry.class);
    hookRegistry.register(injector().getInstance(CommandRequestHook.class));
    hookRegistry.register(injector().getInstance(CommandResponseHook.class));
    hookRegistry.register(injector().getInstance(NotificationRequestHook.class));
    hookRegistry.register(injector().getInstance(NotificationResponseHook.class));
  }

  @Override
  public void disable() {

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