package net.taskwolf.device.access;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.security.Key;
import java.util.UUID;
import java.util.function.Consumer;

@Accessors(fluent = true)
public class DeviceController extends TaskwolfRestController {
  @Getter(AccessLevel.PROTECTED)
  private final DeviceDatabaseTable deviceDatabaseTable;

  protected DeviceController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.deviceDatabaseTable = deviceDatabaseTable;
  }

  protected void performDeviceOperation(
    UUID userId, String deviceId, Consumer<Device> operation, Runnable failResponse
  ) {
    deviceDatabaseTable.deviceExists(deviceId).thenAccept(exists ->
      performDeviceOperation(userId, deviceId, exists, operation, failResponse));
  }

  private void performDeviceOperation(
    UUID userId, String deviceId, boolean deviceExists,
    Consumer<Device> operation, Runnable failResponse
  ) {
    if (!deviceExists) {
      failResponse.run();
      return;
    }
    deviceDatabaseTable.findDevice(deviceId).thenAccept(device ->
      performDeviceOperation(userId, device, operation, failResponse));
  }

  private void performDeviceOperation(
    UUID userId, Device device, Consumer<Device> operation, Runnable failResponse
  ) {
    if (!device.ownerId().equals(userId)) {
      failResponse.run();
      return;
    }
    operation.accept(device);
  }
}