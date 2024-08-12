package net.taskwolf.device.trigger.folder.create;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.*;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerContentDatabaseTable;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceFolderCreateTrigger implements Trigger {
  public static DeviceFolderCreateTrigger create(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("workspace", DatabaseDataType.UUID));
    return new DeviceFolderCreateTrigger(deviceComponentSelect, workspaceComponentSelect,
      TriggerContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "trigger_device_folder_create", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-folder-create-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("device.trigger.folder.create.name")
      .withDescription("device.trigger.folder.create.description")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.folder.create.input.device.name",
        "device", "device.trigger.folder.create.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.create.input.workspace.name",
        "workspace", "device.trigger.folder.create.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.folder.path", "folderPath"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
    contentDatabaseTable.createIndexIfNotExists("device");
    contentDatabaseTable.createIndexIfNotExists("workspace");
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(
      content.get("device"), UUID.fromString((String) content.get("workspace"))));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(String condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
