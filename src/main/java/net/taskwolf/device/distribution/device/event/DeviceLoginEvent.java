package net.taskwolf.device.distribution.device.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DeviceLoginEvent extends Event {
  private final String deviceId;
  private final DistributionClient client;
}
