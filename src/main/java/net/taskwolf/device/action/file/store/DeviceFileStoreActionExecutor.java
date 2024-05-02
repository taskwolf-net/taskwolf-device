package net.taskwolf.device.action.file.store;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspace;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileStoreActionExecutor implements ActionExecutor {
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
