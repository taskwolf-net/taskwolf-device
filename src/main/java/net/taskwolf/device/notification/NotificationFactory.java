package net.taskwolf.device.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreModule;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.trigger.DeviceTriggerFactory;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationFactory {
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;
  private final CoreModule coreModule;
  private final DeviceTriggerFactory deviceTriggerFactory;

  public Notification createNotification(Device device, String title, String body) {
    return Notification.create(firebaseDeviceDatabaseTable,
      deviceConfiguration, clientRegistry, coreModule, deviceTriggerFactory,
      device, title, body);
  }
}
