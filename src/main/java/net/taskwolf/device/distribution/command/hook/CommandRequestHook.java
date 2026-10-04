package net.taskwolf.device.distribution.command.hook;

import net.taskwolf.device.connection.DeviceConnectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.command.event.WorkerCommandRequestEvent;

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
