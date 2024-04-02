package net.taskwolf.device.structure;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DeviceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device";

  public static DeviceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("machine", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("information", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    return new DeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private final Random random = new Random();

  private DeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertDevice(Device device) {
    insertDevice(device.id(), device.machineId(), device.ownerId(),
      device.information(), device.type());
  }

  public void insertDevice(
    String id, String machineId, UUID ownerId, String information, String type
  ) {
    insert(DatabaseRow.of(id, machineId, ownerId, information, type));
  }

  public void deleteDevice(String deviceId) {
    delete(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<String> generateAvailableDeviceId() {
    var futureResponse = new CompletableFuture<String>();
    var id = createDeviceId();
    deviceExists(id).thenApply(exists -> exists ?
      generateAvailableDeviceId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

  private String createDeviceId() {
    var value = new StringBuilder();
    for (int i = 0; i < 32; i++) {
      value.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return value.toString();
  }

  public CompletableFuture<Boolean> deviceExists(String deviceId) {
    return exists(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<Boolean> deviceExists(String machineId, UUID ownerId) {
    return exists("machine='" + machineId + "' AND owner=" + ownerId + " ALLOW FILTERING");
  }

  public CompletableFuture<Device> findDevice(String deviceId) {
    return selectRow(DatabaseCell.create(deviceId)).thenApply(Device::of);
  }

  public CompletableFuture<Device> findDevice(String machineId, UUID ownerId) {
    return selectRow("machine='" + machineId + "' AND owner=" + ownerId +
      " ALLOW FILTERING").thenApply(Device::of);
  }
}
