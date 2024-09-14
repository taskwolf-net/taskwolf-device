package com.dulno.device.distribution.notification.hook;

import com.dulno.device.distribution.notification.event.WorkerNotificationResponseEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationResponseHook implements Hook {
  @EventHook
  private void notificationResponse(WorkerNotificationResponseEvent event) {
    //CURRENTLY UNUSED
    //COULD LATER BE USED TO COLLECT STATISTICS ABOUT NOTIFICATIONS
  }
}
