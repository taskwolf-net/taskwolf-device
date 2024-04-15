package net.taskwolf.device.distribution.file.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileInfoResponseEvent extends Event {
  private final UUID infoId;
  private final byte[] content;
  private final boolean success;
}
