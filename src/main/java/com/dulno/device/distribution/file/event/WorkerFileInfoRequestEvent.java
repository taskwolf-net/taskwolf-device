package com.dulno.device.distribution.file.event;

import com.dulno.device.structure.DevicePlatform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerFileInfoRequestEvent extends Event {
  private final UUID infoId;
  private final String deviceId;
  private final DevicePlatform devicePlatform;
  private final String filePath;
  private final String fileName;
}
