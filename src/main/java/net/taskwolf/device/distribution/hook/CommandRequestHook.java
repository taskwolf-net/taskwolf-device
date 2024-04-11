package net.taskwolf.device.distribution.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.distribution.event.CommandRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;

  @EventHook
  private void commandRequest(CommandRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    connection.get().executeCommand(event.commandId(), event.command());
  }
}
