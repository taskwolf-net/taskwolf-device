package net.taskwolf.device.action.folder.delete;

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
import net.taskwolf.device.action.file.delete.DeviceFileDeleteAction;
import net.taskwolf.device.action.file.delete.DeviceFileDeleteActionExecutor;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFolderDeleteAction implements Action<DeviceFolderDeleteActionExecutor> {
  public static DeviceFolderDeleteAction create(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect,
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("workspace", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("folderPath", DatabaseDataType.TEXT));
    return new DeviceFolderDeleteAction(deviceComponentSelect, workspaceComponentSelect,
      deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_folder_delete", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-folder-delete-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.folder.delete.name")
      .withDescription("device.action.folder.delete.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.folder.delete.input.device.name",
        "device", "device.action.folder.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.folder.delete.input.workspace.name",
        "workspace", "device.action.folder.delete.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.folder.delete.input.folder.path.name",
        "folderPath", "device.action.folder.delete.input.folder.path.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.delete.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.delete.output.folder.path", "folderPath"))
      .build();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), content.get("workspace"), content.get("folderPath")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue(),
        "folderPath", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFolderDeleteActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFolderDeleteActionExecutor.create(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, content.findCell(1).stringValue(),
        content.findCell(2).uuidValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
