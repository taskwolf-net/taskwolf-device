package com.dulno.device.action.command;

import com.dulno.device.command.CommandFactory;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import lombok.AllArgsConstructor;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceCommandActionExecutor implements ActionExecutor {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final CommandFactory commandFactory;
  private final String deviceId;
  private String command;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    command = dissolve.dissolve(command);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::executeCommand);
  }

  private CompletableFuture<ActionResult> executeCommand(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.command.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::executeCommand);
  }

  private CompletableFuture<ActionResult> executeCommand(Device device) {
    if (!device.commandExecution()) {
      return ActionResult.futureFailure("device.action.command.failure.device.permission");
    }
    var futureResponse = new CompletableFuture<ActionResult>();
    commandFactory.createCommand(device, command).execute(futureResponse);
    return futureResponse;
  }
}
