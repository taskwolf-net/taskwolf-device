package net.taskwolf.device.trigger.file.delete;

import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import net.taskwolf.device.trigger.TriggerWorkspaceDatabaseTable;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.workflow.trigger.Trigger;
import net.taskwolf.workflow.trigger.TriggerInformation;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceFileDeleteTrigger implements Trigger {
  public static DeviceFileDeleteTrigger create(
    UserDeviceDatabaseTable deviceDatabaseTable,
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    return new DeviceFileDeleteTrigger(deviceDatabaseTable, deviceComponentSelect,
      workspaceComponentSelect,
      TriggerWorkspaceDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_device_file_delete"));
  }

  private final UserDeviceDatabaseTable deviceDatabaseTable;
  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final TriggerWorkspaceDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-file-delete-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("device.trigger.file.delete.name")
      .withDescription("device.trigger.file.delete.description")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.delete.input.device.name",
        "device", "device.trigger.file.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.delete.input.workspace.name",
        "workspace", "device.trigger.file.delete.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.file.name", "fileName"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.initialize();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID triggerId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(triggerId, ownerId,
      (String) content.get("device"),
      UUID.fromString((String) content.get("workspace")));
  }

  @Override
  public CompletableFuture<Boolean> checkExecution(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId)
      .thenCompose(row -> deviceDatabaseTable.userHasDevice(
        row.findCell(3).uuidValue(), row.findCell(0).stringValue()));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(0).stringValue(),
        "workspace", row.findCell(1).uuidValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(2).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
