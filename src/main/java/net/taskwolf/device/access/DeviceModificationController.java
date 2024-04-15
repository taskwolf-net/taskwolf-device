package net.taskwolf.device.access;

import com.google.common.hash.Hashing;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.DevicePlatform;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class DeviceModificationController extends DeviceController {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;

  private DeviceModificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
  }

  @RequestMapping(path = "/device/login/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> deviceLogin(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var information = body.getString("information");
    var platform = DevicePlatform.valueOf(body.getString("platform").toUpperCase());
    var firebaseToken = platform.isAndroid() ? body.getString("firebaseToken") : "";
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user ->
      deviceDatabaseTable().deviceExists(deviceId, user.id()).thenAccept(exists ->
        deviceLogin(user, deviceId, information, platform, firebaseToken, exists)
          .thenAccept(futureResponse::complete)));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> deviceLogin(
    User user, String deviceId, String information, DevicePlatform platform,
    String firebaseToken, boolean exists
  ) {
    if (exists) {
      return deviceDatabaseTable().findDevice(deviceId, user.id())
        .thenApply(device -> existingDeviceLogin(device, platform, firebaseToken));
    }
    var futureId = deviceDatabaseTable().generateAvailableDeviceId();
    futureId.thenAccept(id -> newDeviceLogin(user, deviceId, information,
      platform, firebaseToken, id));
    return futureId.thenApply(id -> Map.of("id", id));
  }

  private Map<String, Object> existingDeviceLogin(
    Device device, DevicePlatform platform, String firebaseToken
  ) {
    if (platform.isMobile()) {
      firebaseDeviceDatabaseTable.storeDeviceIdentifier(device.id(), firebaseToken);
    }
    return Map.of("id", device.id());
  }

  private void newDeviceLogin(
    User user, String deviceId, String information, DevicePlatform platform,
    String firebaseToken, String id
  ) {
    deviceDatabaseTable().insertDevice(id, deviceId, user.id(), information,
      platform.toString(), user.language(), true, true, false, true, true, true);
    userDeviceDatabaseTable.addDevice(user.id(), id);
    if (platform.isMobile()) {
      firebaseDeviceDatabaseTable.storeDeviceIdentifier(id, firebaseToken);
    }
  }

  @RequestMapping(path = "/device/organization/add/", method = RequestMethod.POST)
  public void addDeviceToOrganization(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var organizationId = body.getUUID("organization");
    findUser(request).thenAccept(user ->
      deviceDatabaseTable().deviceExists(deviceId).thenAccept(exists ->
        addDeviceToOrganization(user, deviceId, organizationId, exists)));
  }

  private void addDeviceToOrganization(
    User user, String deviceId, UUID organizationId, boolean deviceExists
  ) {
    if (!deviceExists) {
      return;
    }
    if (!user.organizations().contains(organizationId)) {
      return;
    }
    if (user.id().equals(organizationId)) {
      return;
    }
    deviceDatabaseTable().findDevice(deviceId).thenAccept(device ->
      userDeviceDatabaseTable.addDevice(organizationId, device.id()));
  }

  @RequestMapping(path = "/device/organization/remove/", method = RequestMethod.POST)
  public void removeDeviceFromOrganization(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var organizationId = body.getUUID("organization");
    findUser(request).thenAccept(user ->
      deviceDatabaseTable().deviceExists(deviceId).thenAccept(exists ->
        removeDeviceFromOrganization(user, deviceId, organizationId, exists)));
  }

  private void removeDeviceFromOrganization(
    User user, String deviceId, UUID organizationId, boolean deviceExists
  ) {
    if (!deviceExists) {
      return;
    }
    if (!user.organizations().contains(organizationId)) {
      return;
    }
    if (user.id().equals(organizationId)) {
      return;
    }
    deviceDatabaseTable().findDevice(deviceId).thenAccept(device ->
      userDeviceDatabaseTable.removeDevice(organizationId, device.id()));
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
    if (!user.passwordHash().equals(hashPassword(password))) {
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
    if (!target.passwordHash().equals(hashPassword(newAccountPassword))) {
      return Map.of("success", false, "errorCode", 1002);
    }
    removeDeviceFromUsers(device).thenAccept(value ->
      userDeviceDatabaseTable.addDevice(target.id(), device.id()));
    deviceDatabaseTable().changeDeviceOwner(device, target.id());
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
    if (!user.passwordHash().equals(hashPassword(password))) {
      return Map.of("success", false);
    }
    deviceDatabaseTable().deleteDevice(device.id());
    removeDeviceFromUsers(device);
    return Map.of("success", true);
  }

  private CompletableFuture<Void> removeDeviceFromUsers(Device device) {
    return userDeviceDatabaseTable.findUsersOfDevice(device.id())
      .thenAccept(users -> users.forEach(target ->
        userDeviceDatabaseTable.removeDevice(target, device.id())));
  }

  private String hashPassword(String password) {
    return Hashing.sha256().hashString(password, StandardCharsets.UTF_8)
      .toString();
  }
}
