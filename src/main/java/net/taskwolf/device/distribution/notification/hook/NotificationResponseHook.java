package net.taskwolf.device.distribution.notification.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.notification.event.NotificationResponseEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NotificationResponseHook implements Hook {
  @EventHook
  private void notificationResponse(NotificationResponseEvent event) {
    //CURRENTLY UNUSED
    //COULD LATER BE USED TO COLLECT STATISTICS ABOUT NOTIFICATIONS
  }
}
