package net.taskwolf.device.distribution.command.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CommandResponseEvent extends Event {
  private final UUID commandId;
  private final boolean delivered;
  private final String output;
  private final String errorMessage;
  private final int exitCode;
}