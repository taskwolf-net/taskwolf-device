package net.taskwolf.device.notification;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.notification.packet.outgoing.PacketOutgoingNotificationRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;
  private final CoreModule coreModule;
  private final Device device;
  private final String title;
  private final String body;

  public void publish() {
    if (device.platform().isMobile()) {
      publishMobileNotification();
    } else {
      publishDesktopNotification();
    }
    triggerWorkflows();
  }

  private void publishMobileNotification() {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(this::publishMobileNotification);
  }

  private void publishMobileNotification(String identifier) {
    FirebaseRequest.create(deviceConfiguration, identifier).send("notification",
      Map.of("title", title, "body", body));
  }

  private void publishDesktopNotification() {
    //TODO: GENERATE VALID NOTIFICATION ID (FOR STATISTICS / NOTIFICATION HISTORY)
    publishDesktopNotification(UUID.randomUUID());
  }

  private void publishDesktopNotification(UUID notificationId) {
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingNotificationRequest(notificationId,
        device.id(), title, body));
  }

  private void triggerWorkflows() {
    coreModule.triggerWorkflows("device", "device-notification-trigger",
      "device='" + device.id() + "'", triggerInformation());
  }

  private Map<String, Object> triggerInformation() {
    var information = device.composition();
    information.put("notificationTitle", title);
    information.put("notificationBody", body);
    return information;
  }
}
