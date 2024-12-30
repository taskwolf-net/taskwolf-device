package com.dulno.device.action.file.info;

import com.dulno.device.file.FileFactory;
import com.dulno.device.file.workspace.FileWorkspace;
import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.dulno.device.structure.UserDeviceDatabaseTable;
import lombok.AllArgsConstructor;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileInfoActionExecutor implements ActionExecutor {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;
  private final FileFactory fileFactory;
  private final UUID ownerId;
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
      .thenCompose(this::checkDeviceExistence);
  }

  private CompletableFuture<ActionResult> checkDeviceExistence(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.file.info.failure.device.not.found");
    }
    return userDeviceDatabaseTable.userHasDevice(ownerId, deviceId)
      .thenCompose(this::checkDeviceAccess);
  }

  private CompletableFuture<ActionResult> checkDeviceAccess(boolean hasPermission) {
    if (!hasPermission) {
      return ActionResult.futureFailure("device.action.file.info.failure.device.access");
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
