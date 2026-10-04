package net.taskwolf.device.notification;

import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.workflow.WorkflowModule;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.worker.client.WorkerProxyClient;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationFactory {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final WorkerProxyClient workerProxyClient;
  private final WorkflowModule workflowModule;

  public Notification createNotification(Device device, String title, String body) {
    return Notification.create(firebaseDeviceDatabaseTable, deviceConfiguration,
      googleCredentials, workerProxyClient, workflowModule, device, title, body);
  }
}
