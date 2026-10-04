package net.taskwolf.device.distribution.notification.hook;

import net.taskwolf.device.distribution.notification.event.WorkerNotificationResponseEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationResponseHook implements Hook {
  @EventHook
  private void notificationResponse(WorkerNotificationResponseEvent event) {
    //CURRENTLY UNUSED
    //COULD LATER BE USED TO COLLECT STATISTICS ABOUT NOTIFICATIONS
  }
}
