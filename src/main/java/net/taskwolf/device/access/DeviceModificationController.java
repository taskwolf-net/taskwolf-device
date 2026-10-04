package net.taskwolf.device.access;

import net.taskwolf.core.hashing.Hashing;
import net.taskwolf.device.command.CommandExecutionDatabaseTable;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;
import net.taskwolf.device.structure.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.organization.team.TeamDatabaseTable;
import net.taskwolf.core.organization.team.TeamTargetDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.user.UserTargetDatabaseTable;
import net.taskwolf.core.user.activity.ActivityType;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.core.user.activity.UserActivityDatabaseTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class DeviceModificationController extends DeviceController {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final UserActivityDatabaseTable activityDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;
  private final FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable;
  private final Hashing hashing;

  private DeviceModificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    UserActivityDatabaseTable activityDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    @Qualifier("fileStorageDatabaseTable")
    FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Qualifier("fileInfoDatabaseTable")
    FileHistoryDatabaseTable fileInfoDatabaseTable,
    @Qualifier("fileDeleteDatabaseTable")
    FileHistoryDatabaseTable fileDeleteDatabaseTable,
    FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable, Hashing hashing
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.activityDatabaseTable = activityDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.commandExecutionDatabaseTable = commandExecutionDatabaseTable;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
    this.fileWorkspaceDatabaseTable = fileWorkspaceDatabaseTable;
    this.hashing = hashing;
  }

  @RequestMapping(path = "/device/login/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> deviceLogin(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var information = body.getSanitizedString("information", 64);
    var platform = DevicePlatform.valueOf(body.getString("platform").toUpperCase());
    var firebaseToken = platform.isAndroid() ? body.getString("firebaseToken") : "";
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user ->
      deviceDatabaseTable().deviceExists(deviceId, user.id()).thenAccept(exists ->
        deviceLogin(user, deviceId, information, platform, firebaseToken, exists)
          .thenAccept(futureResponse::complete)));
    return futureResponse;
  }

  public CompletableFuture<Map<String, Object>> deviceLogin(
    User user, String deviceId, String information, DevicePlatform platform,
    String firebaseToken, boolean exists
  ) {
    if (exists) {
      return deviceDatabaseTable().findDevice(deviceId, user.id())
        .thenApply(device -> existingDeviceLogin(device, platform, firebaseToken));
    }
    return deviceDatabaseTable().generateAvailableDeviceId()
      .thenCompose(id -> newDeviceLogin(user, deviceId, information,
        platform, firebaseToken, id));
  }

  private Map<String, Object> existingDeviceLogin(
    Device device, DevicePlatform platform, String firebaseToken
  ) {
    if (platform.isMobile()) {
      firebaseDeviceDatabaseTable.storeDeviceIdentifier(device.id(), firebaseToken);
    }
    return Map.of("id", device.id());
  }

  private CompletableFuture<Map<String, Object>> newDeviceLogin(
    User user, String deviceId, String information, DevicePlatform platform,
    String firebaseToken, String id
  ) {
    if (platform.isMobile()) {
      firebaseDeviceDatabaseTable.storeDeviceIdentifier(id, firebaseToken);
    }
    activityDatabaseTable.insertActivity(user.id(), "activity.device.new.title",
      "activity.device.new.description", ActivityType.DEVICE);
    return deviceDatabaseTable().insertDevice(id, deviceId, user.id(),
      information, platform.toString(), user.language(), true, true, true, true,
      true, true, true, true, true)
      .thenCompose(value -> userDeviceDatabaseTable.insertUserDevice(
        UserDevice.create(user.id(), id, user.id(), information, platform)))
      .thenApply(value -> Map.of("id", id));
  }

  @RequestMapping(path = "/device/organization/add/", method = RequestMethod.POST)
  public void addDeviceToOrganization(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var userId = findUserId(request);
    var deviceId = body.getString("device");
    performDeviceOrganizationOperation(userId, deviceId,
      body.getUUID("organization"), body.getUUID("team"), target ->
      addDeviceToOrganization(userId, deviceId, target), () -> {});
  }

  private void addDeviceToOrganization(
    UUID userId, String deviceId, UUID targetId
  ) {
    deviceDatabaseTable().findDevice(deviceId).thenAccept(device ->
      userDeviceDatabaseTable.insertUserDevice(targetId, device.id(),
        device.ownerId(), device.information(), device.platform()));
    activityDatabaseTable.insertActivity(userId, "activity.device.organization.add.title",
      "activity.device.organization.add.description", ActivityType.DEVICE);
  }

  @RequestMapping(path = "/device/organization/remove/", method = RequestMethod.POST)
  public void removeDeviceFromOrganization(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var userId = findUserId(request);
    var deviceId = body.getString("device");
    performDeviceOrganizationOperation(userId, deviceId,
      body.getUUID("organization"), body.getUUID("team"), target ->
        removeDeviceFromOrganization(userId, deviceId, target), () -> {});
  }

  private void removeDeviceFromOrganization(
    UUID userId, String deviceId, UUID targetId
  ) {
    deviceDatabaseTable().findDevice(deviceId).thenAccept(device ->
      userDeviceDatabaseTable.deleteUserDevice(targetId, device.id()));
    activityDatabaseTable.insertActivity(userId, "activity.device.organization.remove.title",
      "activity.device.organization.remove.description", ActivityType.DEVICE);
  }

  @RequestMapping(path = "/device/language/change/", method = RequestMethod.POST)
  public void changeLanguage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId, device ->
      deviceDatabaseTable().updateDeviceLanguage(device,
        body.getString("language")), () -> {});
  }

  @RequestMapping(path = "/device/account/change/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> changeDeviceAccount(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var newEmail = body.getString("newAccountEmail");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user -> performDeviceOperation(user.id(),
      body.getString("device"), device -> userDatabaseTable().userExists(newEmail)
        .thenAccept(exists -> changeDeviceAccount(user, device,
          body.getString("password"), newEmail, body.getString("newAccountPassword"),
          exists).thenAccept(futureResponse::complete)),
      () -> futureResponse.complete(Map.of("success", false))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> changeDeviceAccount(
    User user, Device device, String password, String newAccountEmail,
    String newAccountPassword, boolean exists
  ) {
    if (!hashing.matches(password, user.passwordHash())) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1000));
    }
    if (!exists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1001));
    }
    return userDatabaseTable().findUser(newAccountEmail).thenApply(target ->
      changeDeviceAccount(device, newAccountPassword, target));
  }

  private Map<String, Object> changeDeviceAccount(
    Device device, String newAccountPassword, User target
  ) {
    if (!hashing.matches(newAccountPassword, target.passwordHash())) {
      return Map.of("success", false, "errorCode", 1002);
    }
    removeDeviceFromUsers(device);
    deviceDatabaseTable().changeDeviceOwner(device, target.id());
    return Map.of("success", true);
  }

  @RequestMapping(path = "/device/rename/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> renameDevice(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var information = body.getSanitizedString("information", 64);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user -> performDeviceOperation(user.id(),
      deviceId, device -> futureResponse.complete(
        renameDevice(device, information)),
      () -> futureResponse.complete(Map.of("success", false))));
    return futureResponse;
  }

  private Map<String, Object> renameDevice(
    Device device, String information
  ) {
    deviceDatabaseTable().renameDevice(device, information);
    userDeviceDatabaseTable.findUserDevicesById(device.id())
      .thenAccept(userDevices -> userDevices.forEach(userDevice ->
        userDeviceDatabaseTable.renameUserDevice(userDevice, information)));
    return Map.of("success", true);
  }

  @RequestMapping(path = "/device/delete/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> deleteDevice(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user -> performDeviceOperation(user.id(),
      body.getString("device"), device -> futureResponse.complete(
        deleteDevice(user, device, body.getString("password"))),
      () -> futureResponse.complete(Map.of("success", false))));
    return futureResponse;
  }

  private Map<String, Object> deleteDevice(
    User user, Device device, String password
  ) {
    if (!hashing.matches(password, user.passwordHash())) {
      return Map.of("success", false);
    }
    deleteDevice(device);
    activityDatabaseTable.insertActivity(user.id(), "activity.device.delete.title",
      "activity.device.delete.description", ActivityType.DEVICE);
    return Map.of("success", true);
  }

  public void deleteDevice(Device device) {
    deviceDatabaseTable().deleteDevice(device.id());
    removeDeviceFromUsers(device);
    firebaseDeviceDatabaseTable.deleteDeviceIdentifier(device.id());
    commandExecutionDatabaseTable.findExecutionsOfDevice(device.id())
      .thenAccept(executions -> executions.forEach(execution ->
        commandExecutionDatabaseTable.deleteCommandExecution(execution.id())));
    deleteFileHistory(fileStorageDatabaseTable, device.id());
    deleteFileHistory(fileInfoDatabaseTable, device.id());
    deleteFileHistory(fileDeleteDatabaseTable, device.id());
    fileWorkspaceDatabaseTable.findWorkspacesOfDevice(device.id())
      .thenAccept(workspaces -> workspaces.forEach(workspace ->
        fileWorkspaceDatabaseTable.deleteWorkspace(workspace.id())));
  }

  private void deleteFileHistory(FileHistoryDatabaseTable table, String deviceId) {
    table.findEntriesOfDevice(deviceId).thenAccept(entries ->
      entries.forEach(entry -> table.deleteEntry(entry.id())));
  }

  private void removeDeviceFromUsers(Device device) {
    userDeviceDatabaseTable.findUsersOfDevice(device.id())
      .thenAccept(users -> users.forEach(target ->
        userDeviceDatabaseTable.deleteUserDevice(target, device.id())));
  }
}
