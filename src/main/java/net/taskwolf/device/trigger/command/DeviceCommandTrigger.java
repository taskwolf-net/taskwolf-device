package net.taskwolf.device.trigger.command;

import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.workflow.trigger.Trigger;
import net.taskwolf.workflow.trigger.TriggerContentDatabaseTable;
import net.taskwolf.workflow.trigger.TriggerInformation;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceCommandTrigger implements Trigger {
  public static DeviceCommandTrigger create(
    UserDeviceDatabaseTable deviceDatabaseTable,
    InputComponentSelect deviceComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    return new DeviceCommandTrigger(deviceDatabaseTable, deviceComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_device_command", contentColumns));
  }

  private final UserDeviceDatabaseTable deviceDatabaseTable;
  private final InputComponentSelect deviceComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-command-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("device.trigger.command.name")
      .withDescription("device.trigger.command.description")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.command.input.device.name",
        "device", "device.trigger.command.input.device.description", deviceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command", "command"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.output", "commandOutput"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.error.message", "commandErrorMessage"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.exit.code", "commandExitCode"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.execution.time", "commandExecutionTime"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("device");
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID triggerId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(ownerId,
      content.get("device")));
  }

  @Override
  public CompletableFuture<Boolean> checkExecution(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId)
      .thenCompose(row -> deviceDatabaseTable.userHasDevice(
        row.findCell(1).uuidValue(), row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
