package com.dulno.device.action.file.info;

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
public final class DeviceFileInfoAction implements Action<DeviceFileInfoActionExecutor> {
  public static DeviceFileInfoAction create(
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
    return new DeviceFileInfoAction(deviceComponentSelect, workspaceComponentSelect,
      deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_device_file_info", contentColumns));
  }

  private final InputComponentSelect deviceComponentSelect;
  private final InputComponentSelect workspaceComponentSelect;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "device-file-info-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("device.action.file.info.name")
      .withDescription("device.action.file.info.description")
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.info.input.device.name",
        "device", "device.action.file.info.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.info.input.workspace.name",
        "workspace", "device.action.file.info.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createOptional("device.action.file.info.input.file.path.name",
        "filePath", "device.action.file.info.input.file.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.info.input.file.name.name",
        "fileName", "device.action.file.info.input.file.name.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.file.name", "fileName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.info.output.file.content", "fileContent"))
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
      filePath == null ? "" : content.get("filePath"), content.get("fileName")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("device", row.findCell(1).stringValue(),
        "workspace", row.findCell(2).uuidValue(),
        "filePath", row.findCell(3).stringValue(),
        "fileName", row.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<DeviceFileInfoActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      DeviceFileInfoActionExecutor.create(deviceDatabaseTable,
        workspaceDatabaseTable, fileFactory, content.findCell(1).stringValue(),
        content.findCell(2).uuidValue(), content.findCell(3).stringValue(),
        content.findCell(4).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
