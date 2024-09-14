package com.dulno.device.action.file.store;

import com.dulno.device.file.FileFactory;
import com.dulno.device.file.workspace.FileWorkspace;
import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import lombok.AllArgsConstructor;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
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
