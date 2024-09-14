package com.dulno.device.distribution.notification.hook;

import com.dulno.device.connection.DeviceConnectionRepository;
import com.dulno.device.distribution.notification.event.WorkerNotificationRequestEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;

  @EventHook
  private void notificationRequest(WorkerNotificationRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    connection.get().sendNotification(event.title(), event.body());
  }
}