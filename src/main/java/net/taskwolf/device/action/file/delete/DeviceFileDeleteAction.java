package net.taskwolf.device.action.file.delete;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionContentDatabaseTable;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.device.action.file.info.DeviceFileInfoAction;
import net.taskwolf.device.action.file.info.DeviceFileInfoActionExecutor;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileDeleteAction implements Action<DeviceFileDeleteActionExecutor> {
  public static DeviceFileDeleteAction create(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect,
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("workspace", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("filePath", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("fileName", DatabaseDataType.TEXT));
    return new DeviceFileDeleteAction(deviceComponentSelect, workspaceComponentSelect,
      deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_file_delete", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-file-delete-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.file.delete.name")
      .withDescription("device.action.file.delete.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.delete.input.device.name",
        "device", "device.action.file.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.delete.input.workspace.name",
        "workspace", "device.action.file.delete.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createOptional("device.action.file.delete.input.file.path.name",
        "filePath", "device.action.file.delete.input.file.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.delete.input.file.name.name",
        "fileName", "device.action.file.delete.input.file.name.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.file.name", "fileName"))
      .build();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), content.get("workspace"), content.get("filePath"),
      content.get("fileName")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue(),
        "filePath", row.findCell(3).stringValue(),
        "fileName", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFileDeleteActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFileDeleteActionExecutor.create(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, content.findCell(1).stringValue(),
        content.findCell(2).uuidValue(), content.findCell(3).stringValue(),
        content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
