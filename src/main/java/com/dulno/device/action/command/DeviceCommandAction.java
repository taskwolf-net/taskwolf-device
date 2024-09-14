package com.dulno.device.action.command;

import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.dulno.device.command.CommandFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceCommandAction implements Action<DeviceCommandActionExecutor> {
  public static DeviceCommandAction create(
          InputComponentSelect deviceComponentSelect,
          DeviceDatabaseTable deviceDatabaseTable, CommandFactory commandFactory,
          DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("command", DatabaseDataType.TEXT));
    return new DeviceCommandAction(deviceComponentSelect, deviceDatabaseTable,
      commandFactory, ActionContentDatabaseTable.create(databaseConnection,
      databaseKeyspace, "action_device_command", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final CommandFactory commandFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-command-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.command.name")
      .withDescription("device.action.command.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.command.input.device.name",
        "device", "device.action.command.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.command.input.command.name",
        "command", "device.action.command.input.command.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.command", "command"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.command.output", "commandOutput"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.command.error.message", "commandErrorMessage"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.command.exit.code", "commandExitCode"))
      .withOutputVariable(OutputComponentVariable.create("device.action.command.output.command.execution.time", "commandExecutionTime"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), content.get("command")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "command", row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceCommandActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceCommandActionExecutor.create(deviceDatabaseTable, commandFactory,
        content.findCell(1).stringValue(), content.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
