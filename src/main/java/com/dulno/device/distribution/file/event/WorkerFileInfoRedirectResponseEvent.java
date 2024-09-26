package com.dulno.device.distribution.file.event;

import com.dulno.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkerFileInfoRedirectResponseEvent extends Event {
  private final UUID infoId;
  private final String redirectUrl;
}
