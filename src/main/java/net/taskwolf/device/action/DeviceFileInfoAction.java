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
public final class DeviceFileInfoAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.file.info.name")
      .withDescription("device.action.file.info.description")
      .withIdentifier("device-file-info-action")
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

  public static DeviceFileInfoAction of(
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    JSONObject content
  ) {
    return create(deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      content.getString("device"), UUID.fromString(content.getString("workspace")),
      content.has("filePath") ? content.getString("filePath") : "",
      content.getString("fileName"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final String deviceId;
  private final UUID workspaceId;
  private String filePath;
  private String fileName;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    filePath = dissolve.dissolve(filePath);
    fileName = dissolve.dissolve(fileName);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::fileInfo);
  }

  private CompletableFuture<ActionResult> fileInfo(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.file.info.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::fileInfo);
  }

  private CompletableFuture<ActionResult> fileInfo(Device device) {
    if (!device.fileInfo()) {
      return ActionResult.futureFailure("device.action.file.info.failure.device.permission");
    }
    return workspaceDatabaseTable.workspaceExists(workspaceId)
      .thenCompose(exists -> fileInfo(device, exists));
  }

  private CompletableFuture<ActionResult> fileInfo(
    Device device, boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return ActionResult.futureFailure("device.action.file.info.failure.workspace.not.found");
    }
    return workspaceDatabaseTable.findWorkspace(workspaceId)
      .thenCompose(workspace -> fileInfo(device, workspace));
  }

  private CompletableFuture<ActionResult> fileInfo(
    Device device, FileWorkspace workspace
  ) {
    var futureResponse = new CompletableFuture<ActionResult>();
    fileFactory.createFile(device, workspace.path() + filePath, fileName)
      .info(futureResponse);
    return futureResponse;
  }
}
