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
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileStoreAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.file.store.name")
      .withDescription("device.action.file.store.description")
      .withIdentifier("device-file-store-action")
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.store.input.device.name",
        "device", "device.action.file.store.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.store.input.workspace.name",
        "workspace", "device.action.file.store.input.workspace.description", workspaceComponentSelect))
      .withInputVariable(InputComponentVariable.createOptional("device.action.file.store.input.file.path.name",
        "filePath", "device.action.file.store.input.file.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.store.input.file.name.name",
        "fileName", "device.action.file.store.input.file.name.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.store.input.file.content.name",
        "fileContent", "device.action.file.store.input.file.content.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.store.output.file.name", "fileName"))
      .build();
  }

  public static DeviceFileStoreAction of(
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, FileFactory fileFactory,
    JSONObject content
  ) {
    return create(deviceDatabaseTable, workspaceDatabaseTable, fileFactory,
      content.getString("device"), UUID.fromString(content.getString("workspace")),
      content.has("filePath") ? content.getString("filePath") : "",
      content.getString("fileName"), content.getString("fileContent"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final String deviceId;
  private final UUID workspaceId;
  private String filePath;
  private String fileName;
  private String fileContent;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    filePath = dissolve.dissolve(filePath);
    fileName = dissolve.dissolve(fileName);
    fileContent = dissolve.dissolve(fileContent);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::storeFile);
  }

  private CompletableFuture<ActionResult> storeFile(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.file.store.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::storeFile);
  }

  private CompletableFuture<ActionResult> storeFile(Device device) {
    if (!device.fileStorage()) {
      return ActionResult.futureFailure("device.action.file.store.failure.device.permission");
    }
    return workspaceDatabaseTable.workspaceExists(workspaceId)
      .thenCompose(exists -> storeFile(device, exists));
  }

  private CompletableFuture<ActionResult> storeFile(
    Device device, boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return ActionResult.futureFailure("device.action.file.store.failure.workspace.not.found");
    }
    return workspaceDatabaseTable.findWorkspace(workspaceId)
      .thenCompose(workspace -> storeFile(device, workspace));
  }

  private CompletableFuture<ActionResult> storeFile(
    Device device, FileWorkspace workspace
  ) {
    var futureResponse = new CompletableFuture<ActionResult>();
    fileFactory.createFile(device, workspace.path() + filePath, fileName)
      .store(Base64.decodeBase64(fileContent), futureResponse);
    return futureResponse;
  }
}

