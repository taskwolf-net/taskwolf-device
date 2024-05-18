package net.taskwolf.device.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationFactory {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final WorkerProxyClient workerProxyClient;
  private final CoreModule coreModule;

  public Notification createNotification(Device device, String title, String body) {
    return Notification.create(firebaseDeviceDatabaseTable, deviceConfiguration,
      workerProxyClient, coreModule, device, title, body);
  }
}
