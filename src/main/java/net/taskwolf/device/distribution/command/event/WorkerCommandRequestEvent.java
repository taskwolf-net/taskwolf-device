package net.taskwolf.device.distribution.command.event;

import net.taskwolf.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerCommandRequestEvent extends Event {
  private final UUID commandId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String command;
}
