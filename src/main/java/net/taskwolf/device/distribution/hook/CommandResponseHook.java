package net.taskwolf.device.distribution.hook;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.command.CommandRequestRepository;
import net.taskwolf.device.distribution.event.CommandResponseEvent;
import net.taskwolf.device.structure.Device;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandResponseHook implements Hook {
  private final CommandRequestRepository commandRequestRepository;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;

  @EventHook
  private void commandResponse(CommandResponseEvent event) {
    var optionalRequest = commandRequestRepository
      .findCommandRequest(event.commandId());
    if (optionalRequest.isEmpty()) {
      return;
    }
    var request = optionalRequest.get();
    if (!event.delivered()) {
      request.futureResult().complete(ActionResult.failure(
        "device.action.command.failure.device.offline"));
      return;
    }
    long time = System.currentTimeMillis();
    request.futureResult().complete(ActionResult.success(buildInformation(
      request.device(), request.command(), event.output(), event.errorMessage(),
      event.exitCode(), formatTime(time))));
    commandRequestRepository.unregisterCommandRequest(request);
    commandExecutionDatabaseTable.insertCommandExecution(request.id(),
      request.device().id(), time, request.command(),
      event.output(), event.errorMessage(), event.exitCode());
  }

  private Map<String, Object> buildInformation(
    Device device, String command, String commandOutput,
    String commandErrorMessage, int commandExitCode, String commandExecutionTime
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("deviceId", device.id());
    information.put("deviceName", device.information());
    information.put("devicePlatform", device.platform());
    information.put("command", command);
    information.put("commandOutput", commandOutput);
    information.put("commandErrorMessage", commandErrorMessage);
    information.put("commandExitCode", commandExitCode);
    information.put("commandExecutionTime", commandExecutionTime);
    return information;
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
