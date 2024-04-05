package net.taskwolf.device.notification;

import lombok.RequiredArgsConstructor;
import net.taskwolf.device.structure.Device;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final DeviceNotificationDatabaseTable deviceNotificationDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
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
    var body = new JSONObject();
    body.put("to", identifier);
    var notification = new JSONObject();
    notification.put("title", title);
    notification.put("body", body);
    body.put("notification", notification);
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }

  private void publishDesktopNotification() {
    notificationDatabaseTable.generateAvailableNotificationId()
      .thenAccept(this::publishDesktopNotification);
  }

  private void publishDesktopNotification(UUID notificationId) {
    notificationDatabaseTable.insertNotification(notificationId, body, title);
    deviceNotificationDatabaseTable.addNotification(device.id(), notificationId);
  }
}
