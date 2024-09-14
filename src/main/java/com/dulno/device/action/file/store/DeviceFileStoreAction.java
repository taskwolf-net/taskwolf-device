package com.dulno.device.action.file.store;

import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
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
import com.dulno.device.file.FileFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileStoreAction implements Action<DeviceFileStoreActionExecutor> {
  public static DeviceFileStoreAction create(
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
    contentColumns.add(DatabaseColumn.create("fileContent", DatabaseDataType.TEXT));
    return new DeviceFileStoreAction(deviceComponentSelect, workspaceComponentSelect,
      deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_file_store", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-file-store-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.file.store.name")
      .withDescription("device.action.file.store.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.store.input.device.name",
        "device", "device.action.file.store.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.store.input.workspace.name",
        "workspace", "device.action.file.store.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createOptional("device.action.file.store.input.file.path.name",
        "filePath", "device.action.file.store.input.file.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.store.input.file.name.name",
        "fileName", "device.action.file.store.input.file.name.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.store.input.file.content.name",
        "fileContent", "device.action.file.store.input.file.content.description", InputComponentDataType.FILE))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.file.name", "fileName"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    var filePath = content.get("filePath");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("device"), UUID.fromString((String) content.get("workspace")),
      filePath == null ? "" : content.get("filePath"), content.get("fileName"),
      content.get("fileContent")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue(),
        "filePath", row.findCell(3).stringValue(),
        "fileName", row.findCell(4).stringValue(),
        "fileContent", row.findCell(5).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFileStoreActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFileStoreActionExecutor.create(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, content.findCell(1).stringValue(),
        content.findCell(2).uuidValue(), content.findCell(3).stringValue(),
        content.findCell(4).stringValue(), content.findCell(5).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}

