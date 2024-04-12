package net.taskwolf.device.notification;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.packet.outgoing.PacketOutgoingNotificationRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;
  private final Device device;
  private final String title;
  private final String body;

  public void publish() {
    if (device.platform().isMobile()) {
      publishMobileNotification();
    } else {
      publishDesktopNotification();
    }
  }

  private void publishMobileNotification() {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(this::publishMobileNotification);
  }

  private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

  private void publishMobileNotification(String identifier) {
    var requestBody = new JSONObject(Map.of("to", identifier, "notification",
      Map.of("title", title, "body", body)));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "key=" + deviceConfiguration.firebaseToken())
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
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
}
