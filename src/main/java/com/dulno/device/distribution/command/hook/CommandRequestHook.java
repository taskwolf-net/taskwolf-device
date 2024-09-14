package com.dulno.device.distribution.command.hook;

import com.dulno.device.connection.DeviceConnectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.distribution.command.event.WorkerCommandRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;

  @EventHook
  private void commandRequest(WorkerCommandRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    connection.get().executeCommand(event.commandId(), event.command());
  }
}
