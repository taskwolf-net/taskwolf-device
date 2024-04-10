package net.taskwolf.device.distribution.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CommandRequestEvent extends Event {
  private final UUID commandId;
  private final String deviceId;
  private final String command;
  private final DistributionClient client;
}
