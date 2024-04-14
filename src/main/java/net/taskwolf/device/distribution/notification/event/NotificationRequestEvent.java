package net.taskwolf.device.distribution.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NotificationRequestEvent extends Event {
  private final UUID notificationId;
  private final String deviceId;
  private final String title;
  private final String body;
  private final DistributionClient client;
}
