package com.dulno.device.notification;

import com.dulno.device.firebase.FirebaseRequest;
import com.dulno.device.structure.Device;
import com.dulno.workflow.WorkflowModule;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.DeviceConfiguration;
import com.dulno.device.distribution.notification.packet.outgoing.PacketOutgoingNotificationRequest;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final WorkerProxyClient workerProxyClient;
  private final WorkflowModule workflowModule;
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
      .thenAcceptAsync(this::publishMobileNotification);
  }

  private void publishMobileNotification(String identifier) {
    FirebaseRequest.create(deviceConfiguration, googleCredentials, identifier)
      .send("notification", Map.of("title", title, "body", body));
  }

  private void publishDesktopNotification() {
    //TODO: GENERATE VALID NOTIFICATION ID (FOR STATISTICS / NOTIFICATION HISTORY)
    publishDesktopNotification(UUID.randomUUID());
  }

  private void publishDesktopNotification(UUID notificationId) {
    workerProxyClient.sendPacket(new PacketOutgoingNotificationRequest(
      notificationId, device.id(), title, body));
  }

  private void triggerWorkflows() {
    workflowModule.triggerWorkflows("device", "device-notification-trigger",
      DatabaseCondition.of("device", device.id()), triggerInformation(), false);
  }

  private Map<String, Object> triggerInformation() {
    var information = device.composition();
    information.put("notificationTitle", title);
    information.put("notificationBody", body);
    return information;
  }
}
