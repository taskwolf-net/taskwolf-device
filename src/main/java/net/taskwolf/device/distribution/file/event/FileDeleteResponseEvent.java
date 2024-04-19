package net.taskwolf.device.distribution.file.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileDeleteResponseEvent extends Event {
  private final UUID deleteId;
  private final boolean success;
}
