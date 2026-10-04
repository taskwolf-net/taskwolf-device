package net.taskwolf.device.action.folder.create;

import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.device.file.FileFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFolderCreateAction implements Action<DeviceFolderCreateActionExecutor> {
  public static DeviceFolderCreateAction create(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("workspace", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("folderPath", DatabaseDataType.TEXT));
    return new DeviceFolderCreateAction(deviceComponentSelect,
      workspaceComponentSelect, deviceDatabaseTable, userDeviceDatabaseTable,
      workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_folder_create", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-folder-create-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.folder.create.name")
      .withDescription("device.action.folder.create.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.folder.create.input.device.name",
        "device", "device.action.folder.create.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.folder.create.input.workspace.name",
        "workspace", "device.action.folder.create.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.folder.create.input.folder.path.name",
        "folderPath", "device.action.folder.create.input.folder.path.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.create.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.create.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.create.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.create.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.action.folder.create.output.folder.path", "folderPath"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      content.get("device"), UUID.fromString((String) content.get("workspace")),
      content.get("folderPath")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("device", row.findCell(2).stringValue(),
        "workspace", row.findCell(3).uuidValue(),
        "folderPath", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFolderCreateActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFolderCreateActionExecutor.create(deviceDatabaseTable,
        userDeviceDatabaseTable, workspaceDatabaseTable, fileFactory,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).uuidValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
