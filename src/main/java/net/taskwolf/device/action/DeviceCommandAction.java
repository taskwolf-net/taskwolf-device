package net.taskwolf.device.action;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.command.CommandRequest;
import net.taskwolf.device.command.CommandRequestRepository;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceCommandAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.command.name")
      .withDescription("device.action.command.description")
      .withIdentifier("device-command-action")
      .withInputVariable(InputComponentVariable.createSelect("device.action.command.input.device.name",
        "device", "device.action.command.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.command.input.command.name",
        "command", "device.action.command.input.command.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.command", "command"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.command.output", "commandOutput"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.command.error.message", "commandErrorMessage"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.command.exit.code", "commandExitCode"))
      .withOutputVariable(OutputComponentVariable.create("device.action.notification.output.command.execution.time", "commandExecutionTime"))
      .build();
  }

  public static DeviceCommandAction of(
    DeviceDatabaseTable deviceDatabaseTable,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    CommandRequestRepository commandRequestRepository, JSONObject content
  ) {
    return create(deviceDatabaseTable, commandExecutionDatabaseTable,
      commandRequestRepository, content.getString("device"),
      content.getString("command"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CommandRequestRepository commandRequestRepository;
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
    return commandExecutionDatabaseTable.generateAvailableExecutionId()
      .thenCompose(id -> executeCommand(device, id));
  }

  private CompletableFuture<ActionResult> executeCommand(
    Device device, UUID commandId
  ) {
    var futureResponse = new CompletableFuture<ActionResult>();
    commandRequestRepository.registerCommandRequest(CommandRequest.create(
      commandId, device.id(), command, futureResponse));
    return futureResponse;
  }
}
