package net.taskwolf.device.action;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspace;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFolderCreateAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.folder.create.name")
      .withDescription("device.action.folder.create.description")
      .withIdentifier("device-folder-create-action")
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

  public static DeviceFolderCreateAction of(
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    JSONObject content
  ) {
    return create(deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      content.getString("device"), UUID.fromString(content.getString("workspace")),
      content.getString("folderPath"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final String deviceId;
  private final UUID workspaceId;
  private String folderPath;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    folderPath = dissolve.dissolve(folderPath);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::createFolder);
  }

  private CompletableFuture<ActionResult> createFolder(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.folder.create.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::createFolder);
  }

  private CompletableFuture<ActionResult> createFolder(Device device) {
    if (!device.folderCreate()) {
      return ActionResult.futureFailure("device.action.folder.create.failure.device.permission");
    }
    return workspaceDatabaseTable.workspaceExists(workspaceId)
      .thenCompose(exists -> createFolder(device, exists));
  }

  private CompletableFuture<ActionResult> createFolder(
    Device device, boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return ActionResult.futureFailure("device.action.folder.create.failure.workspace.not.found");
    }
    return workspaceDatabaseTable.findWorkspace(workspaceId)
      .thenCompose(workspace -> createFolder(device, workspace));
  }

  private CompletableFuture<ActionResult> createFolder(
    Device device, FileWorkspace workspace
  ) {
    var futureResponse = new CompletableFuture<ActionResult>();
    fileFactory.createFile(device, workspace.path() + folderPath, "")
      .store(new byte[0], futureResponse);
    return futureResponse;
  }
}
