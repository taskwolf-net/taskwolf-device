package net.taskwolf.device.access;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.iterator.AsyncIterator;
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
public final class DeviceInformationController extends TaskwolfRestController {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;

  private DeviceInformationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
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
          findDevice(body.getString("device"), devices)
            .thenAccept(futureResponse::complete))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findDevice(
    String deviceId, List<String> targetDevices
  ) {
    if (!targetDevices.contains(deviceId)) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return deviceDatabaseTable.findDevice(deviceId).thenCompose(
      this::gatherDeviceInformation);
  }

  @RequestMapping(path = "/devices/selected/", method = RequestMethod.GET)
  public CompletableFuture<Map<String, Object>> selectedDevices(
    HttpServletRequest request
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenApply(user ->
      userTargetDatabaseTable.findTargetSecured(user.id()).thenAccept(target ->
        collectDevices(target).thenAccept(devices ->
          collectDevicesInformation(devices).thenApply(futureResponse::complete))));
    return futureResponse;
  }

  private CompletableFuture<List<Device>> collectDevices(
    UUID targetId
  ) {
    var futureResponse = new CompletableFuture<List<Device>>();
    userDeviceDatabaseTable.findDevicesIfExists(targetId).thenAccept(deviceIds ->
      AsyncIterator.execute(deviceIds, deviceDatabaseTable::findDevice,
        deviceIds.size(), futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> collectDevicesInformation(
    List<Device> devices
  ) {
    if (devices.isEmpty()) {
      return CompletableFuture.completedFuture(Map.of("devices",
        Lists.newArrayList()));
    }
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    AsyncIterator.execute(devices, this::gatherDeviceInformation, devices.size(),
      information -> futureResponse.complete(Map.of("devices", information)));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> gatherDeviceInformation(
    Device device
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    userDatabaseTable().findUserIfExists(device.ownerId())
      .thenAccept(owner -> futureResponse.complete(
        assemblyDeviceInformation(device, owner)));
    return futureResponse;
  }

  private Map<String, Object> assemblyDeviceInformation(
    Device device, User owner
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("id", device.id());
    information.put("information", device.information());
    information.put("owner", owner.name());
    return information;
  }
}

