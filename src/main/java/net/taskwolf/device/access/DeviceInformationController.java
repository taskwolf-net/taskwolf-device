package net.taskwolf.device.access;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.user.UserTargetDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class DeviceInformationController extends DeviceController {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;

  private DeviceInformationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
  }

  @RequestMapping(path = "/device/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDevice(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenApply(user ->
      userTargetDatabaseTable.findTargetSecured(user.id()).thenAccept(target ->
        userDeviceDatabaseTable.findDevices(target).thenAccept(devices ->
          findDevice(target, body.getString("device"), devices)
            .thenAccept(futureResponse::complete))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findDevice(
    UUID target, String deviceId, List<String> targetDevices
  ) {
    if (!targetDevices.contains(deviceId)) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return deviceDatabaseTable().findDevice(deviceId).thenCompose(device ->
      gatherDeviceInformation(target, device));
  }

  @RequestMapping(path = "/devices/selected/", method = RequestMethod.GET)
  public CompletableFuture<Map<String, Object>> selectedDevices(
    HttpServletRequest request
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenApply(user ->
      userTargetDatabaseTable.findTargetSecured(user.id()).thenAccept(target ->
        collectDevices(target).thenAccept(devices -> collectDevicesInformation(
          target, devices).thenApply(futureResponse::complete))));
    return futureResponse;
  }

  private CompletableFuture<List<Device>> collectDevices(
    UUID targetId
  ) {
    var futureResponse = new CompletableFuture<List<Device>>();
    userDeviceDatabaseTable.findDevicesIfExists(targetId).thenAccept(deviceIds ->
      AsyncIterator.execute(deviceIds, deviceDatabaseTable()::findDevice,
        deviceIds.size(), futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> collectDevicesInformation(
    UUID target, List<Device> devices
  ) {
    if (devices.isEmpty()) {
      return CompletableFuture.completedFuture(Map.of("devices",
        Lists.newArrayList()));
    }
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    AsyncIterator.execute(devices, device ->
        gatherDeviceInformation(target, device), devices.size(),
      information -> futureResponse.complete(Map.of("devices", information)));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> gatherDeviceInformation(
    UUID target, Device device
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    userDatabaseTable().findUserIfExists(device.ownerId())
      .thenAccept(owner -> futureResponse.complete(
        assemblyDeviceInformation(target, device, owner)));
    return futureResponse;
  }

  private Map<String, Object> assemblyDeviceInformation(
    UUID target, Device device, User owner
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("id", device.id());
    information.put("information", device.information());
    information.put("owner", owner.name());
    information.put("ownDevice", target.equals(device.ownerId()));
    information.put("platform", device.platform());
    return information;
  }

  @RequestMapping(path = "/device/organizations/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDeviceOrganizations(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user -> performDeviceOperation(user.id(),
      deviceId, device -> findDeviceOrganizations(user, device)
        .thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap())));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findDeviceOrganizations(
    User user, Device device
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    userDeviceDatabaseTable.findUsersOfDevice(device.id()).thenApply(users ->
        users.stream().filter(entry -> !entry.equals(user.id())).toList())
      .thenAccept(users -> AsyncIterator.execute(users,
        this::findOrganizationInformation, users.size(), organizations ->
          futureResponse.complete(Map.of("organizations", organizations))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findOrganizationInformation(
    UUID organizationId
  ) {
    return organizationDatabaseTable.findOrganization(organizationId)
      .thenCompose(organization -> userDatabaseTable().findUser(organization.owner())
        .thenApply(owner -> Map.of("id", organization.id(),
          "name", organization.name(), "owner", owner.name())));
  }

  @RequestMapping(path = "/device/language/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDeviceLanguage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> futureResponse.complete(Map.of("language", device.language())),
        () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }
}

