package net.taskwolf.device.access;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.UUID;

@RestController
public final class DeviceModificationController extends TaskwolfRestController {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;

  private DeviceModificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
  }

  @RequestMapping(path = "/device/login/", method = RequestMethod.POST)
  public void deviceLogin(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var information = body.getString("information");
    findUser(request).thenAccept(user ->
      deviceDatabaseTable.deviceExists(deviceId, user.id()).thenAccept(exists ->
        deviceLogin(user, deviceId, information, exists)));
  }

  private void deviceLogin(
    User user, String deviceId, String information, boolean exists
  ) {
    if (exists) {
      return;
    }
    deviceDatabaseTable.generateAvailableDeviceId().thenAccept(id ->
      deviceLogin(user, deviceId, information, id));
  }

  private void deviceLogin(
    User user, String deviceId, String information, String id
  ) {
    deviceDatabaseTable.insertDevice(id, deviceId, user.id(), information);
    userDeviceDatabaseTable.addDevice(user.id(), id);
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
      deviceDatabaseTable.deviceExists(deviceId, user.id()).thenAccept(exists ->
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
    deviceDatabaseTable.findDevice(deviceId, user.id()).thenAccept(device ->
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
      deviceDatabaseTable.deviceExists(deviceId, user.id()).thenAccept(exists ->
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
    deviceDatabaseTable.findDevice(deviceId, user.id()).thenAccept(device ->
      userDeviceDatabaseTable.removeDevice(organizationId, device.id()));
  }

  @RequestMapping(path = "/device/delete/", method = RequestMethod.GET)
  public void deleteDevice(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    findUser(request).thenAccept(user ->
      deviceDatabaseTable.deviceExists(deviceId, user.id()).thenAccept(exists ->
        deleteDevice(user, deviceId, exists)));
  }

  private void deleteDevice(
    User user, String deviceId, boolean exists
  ) {
    if (!exists) {
      return;
    }
    deviceDatabaseTable.findDevice(deviceId, user.id())
      .thenAccept(this::deleteDevice);
  }

  private void deleteDevice(Device device) {
    deviceDatabaseTable.deleteDevice(device.id());
    userDeviceDatabaseTable.findUsersOfDevice(device.id()).thenAccept(users ->
      users.forEach(user -> userDeviceDatabaseTable.removeDevice(user, device.id())));
  }
}
