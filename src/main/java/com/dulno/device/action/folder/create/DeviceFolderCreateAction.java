package com.dulno.device.action.folder.create;

import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.device.file.FileFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFolderCreateAction implements Action<DeviceFolderCreateActionExecutor> {
  public static DeviceFolderCreateAction create(
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
    return new DeviceFolderCreateAction(deviceComponentSelect, workspaceComponentSelect,
      deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_folder_create", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
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
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), UUID.fromString((String) content.get("workspace")),
      content.get("folderPath")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue(),
        "folderPath", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFolderCreateActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFolderCreateActionExecutor.create(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, content.findCell(1).stringValue(),
        content.findCell(2).uuidValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
