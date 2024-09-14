package com.dulno.device.distribution.file.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerFileDeleteRequestEvent extends Event {
  private final UUID deleteId;
  private final String deviceId;
  private final String filePath;
  private final String fileName;
}
