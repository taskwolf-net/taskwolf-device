package net.taskwolf.device.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.structure.Device;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationFactory {
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final DeviceNotificationDatabaseTable deviceNotificationDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;

  public Notification createNotification(Device device, String title, String body) {
    return Notification.create(notificationDatabaseTable,
      deviceNotificationDatabaseTable, firebaseDeviceDatabaseTable,
      deviceConfiguration, device, title, body);
  }
}
