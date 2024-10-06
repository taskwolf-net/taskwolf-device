package com.dulno.device.file.workspace;

import com.dulno.device.access.DeviceController;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dulno.core.CoreModule;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
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
  private final CoreModule coreModule;

  private FileWorkspaceController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    FileWorkspaceDatabaseTable workspaceDatabaseTable, CoreModule coreModule
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.workspaceDatabaseTable = workspaceDatabaseTable;
    this.coreModule = coreModule;
  }

  @RequestMapping(path = "/device/file/workspaces/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileWorkspaces(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
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
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var path = body.getString("path").replace("//", "/").replace("\\\\", "/")
      .replace("\\", "/");
    if (!path.isEmpty() && path.charAt(path.length() - 1) != '/') {
      path += "/";
    }
    if (path.isEmpty()) {
      return;
    }
    var finalPath = path;
    performDeviceOperation(findUserId(request), deviceId,
      device -> workspaceDatabaseTable.generateAvailableWorkspaceId()
        .thenAccept(id -> workspaceDatabaseTable.workspaceExists(deviceId, finalPath)
          .thenAccept(exists -> createFileWorkspace(id, deviceId, finalPath, exists))),
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
    var body = DulnoRequestBody.of(payload, response);
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

  @RequestMapping(path = "/device/file/workspace/file/create/", method = RequestMethod.POST)
  public void workspaceFileCreation(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> fileTrigger("device-file-create-trigger", device,
        body.getUUID("workspace"), body.getString("filePath"),
        body.getString("fileName")), () -> {});
  }

  @RequestMapping(path = "/device/file/workspace/file/delete/", method = RequestMethod.POST)
  public void workspaceFileDeletion(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> fileTrigger("device-file-delete-trigger", device,
        body.getUUID("workspace"), body.getString("filePath"),
        body.getString("fileName")), () -> {});
  }

  private void fileTrigger(
    String identifier, Device device, UUID workspaceId, String filePath,
    String fileName
  ) {
    workspaceDatabaseTable.workspaceExists(workspaceId).thenAccept(exists ->
      fileTrigger(identifier, device, workspaceId, filePath, fileName, exists));
  }

  private void fileTrigger(
    String identifier, Device device, UUID workspaceId, String filePath,
    String fileName, boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return;
    }
    workspaceDatabaseTable.findWorkspace(workspaceId).thenAccept(workspace ->
      fileTrigger(identifier, device, workspace, filePath, fileName));
  }

  private void fileTrigger(
    String identifier, Device device, FileWorkspace workspace, String filePath,
    String fileName
  ) {
    if (!workspace.device().equals(device.id())) {
      return;
    }
    coreModule.triggerWorkflows("device", identifier,
      DatabaseCondition.of("device", device.id(), "workspace", workspace.id(),
        DatabaseCondition.Filtering.ALLOWED),
      fileTriggerInformation(device, workspace.path(), filePath, fileName));
  }

  private Map<String, Object> fileTriggerInformation(
    Device device, String workspace, String filePath, String fileName
  ) {
    var information = device.composition();
    information.put("workspace", workspace);
    information.put("filePath", filePath);
    information.put("fileName", fileName);
    return information;
  }

  @RequestMapping(path = "/device/file/workspace/folder/create/", method = RequestMethod.POST)
  public void workspaceFolderCreation(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> folderTrigger("device-folder-create-trigger", device,
        body.getUUID("workspace"), body.getString("folderPath")), () -> {});
  }

  @RequestMapping(path = "/device/file/workspace/folder/delete/", method = RequestMethod.POST)
  public void workspaceFolderDeletion(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> folderTrigger("device-folder-delete-trigger", device,
        body.getUUID("workspace"), body.getString("folderPath")), () -> {});
  }

  private void folderTrigger(
    String identifier, Device device, UUID workspaceId, String folderPath
  ) {
    workspaceDatabaseTable.workspaceExists(workspaceId).thenAccept(exists ->
      folderTrigger(identifier, device, workspaceId, folderPath, exists));
  }

  private void folderTrigger(
    String identifier, Device device, UUID workspaceId, String folderPath,
    boolean workspaceExists
  ) {
    if (!workspaceExists) {
      return;
    }
    workspaceDatabaseTable.findWorkspace(workspaceId).thenAccept(workspace ->
      folderTrigger(identifier, device, workspace, folderPath));
  }

  private void folderTrigger(
    String identifier, Device device, FileWorkspace workspace, String folderPath
  ) {
    if (!workspace.device().equals(device.id())) {
      return;
    }
    coreModule.triggerWorkflows("device", identifier,
      DatabaseCondition.of("device", device.id(), "workspace", workspace.id(),
        DatabaseCondition.Filtering.ALLOWED),
      folderTriggerInformation(device, workspace.path(), folderPath));
  }

  private Map<String, Object> folderTriggerInformation(
    Device device, String workspace, String folderPath
  ) {
    var information = device.composition();
    information.put("workspace", workspace);
    information.put("folderPath", folderPath);
    return information;
  }
}
