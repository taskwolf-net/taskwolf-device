package net.taskwolf.device.file.workspace;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.access.DeviceController;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class FileWorkspaceController extends DeviceController {
  private final FileWorkspaceDatabaseTable workspaceDatabaseTable;

  private FileWorkspaceController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.workspaceDatabaseTable = workspaceDatabaseTable;
  }

  @RequestMapping(path = "/device/file/workspaces/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileWorkspaces(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> findFileWorkspaces(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findFileWorkspaces(
    Device device
  ) {
    return workspaceDatabaseTable.findWorkspacesOfDevice(device.id())
      .thenApply(workspaces -> Map.of("workspaces", workspaces.stream()
        .map(this::assemblyFileWorkspaceInformation).toList()));
  }

  private Map<String, Object> assemblyFileWorkspaceInformation(
    FileWorkspace workspace
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("id", workspace.id());
    information.put("path", workspace.path());
    return information;
  }

  @RequestMapping(path = "/device/file/workspace/create/", method = RequestMethod.POST)
  public void createFileWorkspace(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var path = body.getString("path");
    if (path.isEmpty()) {
      return;
    }
    performDeviceOperation(findUserId(request), deviceId,
      device -> workspaceDatabaseTable.generateAvailableWorkspaceId()
        .thenAccept(id -> workspaceDatabaseTable.workspaceExists(deviceId, path)
          .thenAccept(exists -> createFileWorkspace(id, deviceId, path, exists))),
      () -> {});
  }

  private void createFileWorkspace(
    UUID id, String deviceId, String path, boolean exists
  ) {
    if (exists) {
      return;
    }
    workspaceDatabaseTable.insertWorkspace(id, deviceId, path);
  }

  @RequestMapping(path = "/device/file/workspace/remove/", method = RequestMethod.POST)
  public void removeFileWorkspace(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var workspaceId = body.getUUID("workspace");
    performDeviceOperation(findUserId(request), deviceId,
      device -> workspaceDatabaseTable.generateAvailableWorkspaceId()
        .thenAccept(id -> workspaceDatabaseTable.workspaceExists(workspaceId)
          .thenAccept(exists -> removeFileWorkspace(deviceId, workspaceId, exists))),
      () -> {});
  }

  private void removeFileWorkspace(
    String deviceId, UUID workspaceId, boolean exists
  ) {
    if (!exists) {
      return;
    }
    workspaceDatabaseTable.findWorkspace(workspaceId).thenAccept(workspace ->
      removeFileWorkspace(deviceId, workspace));
  }

  private void removeFileWorkspace(String deviceId, FileWorkspace workspace) {
    if (!workspace.device().equals(deviceId)) {
      return;
    }
    workspaceDatabaseTable.deleteWorkspace(workspace.id());
  }
}
