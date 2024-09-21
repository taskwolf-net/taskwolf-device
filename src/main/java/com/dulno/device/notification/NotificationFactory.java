package com.dulno.device.notification;

import com.dulno.device.DeviceConfiguration;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;
import com.dulno.device.structure.Device;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.CoreModule;
import com.dulno.core.worker.client.WorkerProxyClient;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationFactory {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final WorkerProxyClient workerProxyClient;
  private final CoreModule coreModule;

  public Notification createNotification(Device device, String title, String body) {
    return Notification.create(firebaseDeviceDatabaseTable, deviceConfiguration,
      googleCredentials, workerProxyClient, coreModule, device, title, body);
  }
}
