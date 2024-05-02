package net.taskwolf.device.action.folder.delete;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.file.workspace.FileWorkspace;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFolderDeleteActionExecutor implements ActionExecutor {
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
      .thenCompose(this::deleteFolder);
  }

  private CompletableFuture<ActionResult> deleteFolder(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.folder.delete.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::deleteFolder);
  }

  private CompletableFuture<ActionResult> deleteFolder(Device device) {
    if (!device.folderDelete()) {
      return ActionResult.futureFailure("device.action.folder.delete.failure.device.permission");
    }
    return workspaceDatabaseTable.workspaceExists(workspaceId)
      .thenCompose(exists -> deleteFolder(device, exists));
  }

  private CompletableFuture<ActionResult> deleteFolder(
    Device device, boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return ActionResult.futureFailure("device.action.folder.delete.failure.workspace.not.found");
    }
    return workspaceDatabaseTable.findWorkspace(workspaceId)
      .thenCompose(workspace -> deleteFolder(device, workspace));
  }

  private CompletableFuture<ActionResult> deleteFolder(
    Device device, FileWorkspace workspace
  ) {
    var futureResponse = new CompletableFuture<ActionResult>();
    fileFactory.createFile(device, workspace.path() + folderPath, "")
      .delete(futureResponse);
    return futureResponse;
  }
}
