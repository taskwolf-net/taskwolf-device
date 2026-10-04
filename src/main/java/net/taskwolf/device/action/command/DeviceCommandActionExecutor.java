package net.taskwolf.device.action.command;

import net.taskwolf.device.command.CommandFactory;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import lombok.AllArgsConstructor;
import net.taskwolf.workflow.action.ActionExecutor;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceCommandActionExecutor implements ActionExecutor {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final CommandFactory commandFactory;
  private final UUID ownerId;
  private final String deviceId;
  private String command;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    command = dissolve.dissolve(command);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::checkDeviceExistence);
  }

  private CompletableFuture<ActionResult> checkDeviceExistence(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.command.failure.device.not.found");
    }
    return userDeviceDatabaseTable.userHasDevice(ownerId, deviceId)
      .thenCompose(this::checkDeviceAccess);
  }

  private CompletableFuture<ActionResult> checkDeviceAccess(boolean hasPermission) {
    if (!hasPermission) {
      return ActionResult.futureFailure("device.action.command.failure.device.access");
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
