package com.dulno.device.distribution.command.hook;

import com.dulno.device.command.CommandExecutionDatabaseTable;
import com.dulno.device.command.CommandRequestRepository;
import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.CoreModule;
import com.dulno.core.action.ActionResult;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.distribution.command.event.WorkerCommandResponseEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandResponseHook implements Hook {
  private final CommandRequestRepository commandRequestRepository;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CoreModule coreModule;

  @EventHook
  private void commandResponse(WorkerCommandResponseEvent event) {
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
    var information = buildInformation(request.device(), request.command(),
      event.output(), event.errorMessage(), event.exitCode(), formatTime(time));
    request.futureResult().complete(ActionResult.success(information));
    commandRequestRepository.unregisterCommandRequest(request);
    commandExecutionDatabaseTable.insertCommandExecution(request.id(),
      request.device().id(), time, request.command(),
      event.output(), event.errorMessage(), event.exitCode());
    triggerWorkflows(request.device(), information);
  }

  private Map<String, Object> buildInformation(
          Device device, String command, String commandOutput,
          String commandErrorMessage, int commandExitCode, String commandExecutionTime
  ) {
    var information = device.composition();
    information.put("command", command);
    information.put("commandOutput", commandOutput);
    information.put("commandErrorMessage", commandErrorMessage);
    information.put("commandExitCode", commandExitCode);
    information.put("commandExecutionTime", commandExecutionTime);
    return information;
  }

  private void triggerWorkflows(Device device, Map<String, Object> information) {
    coreModule.triggerWorkflows("device", "device-command-trigger",
      DatabaseCondition.of("device", device.id()), information);
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
