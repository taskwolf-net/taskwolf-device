package net.taskwolf.device.structure;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserDeviceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_device";

  public static UserDeviceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("user", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("devices", DatabaseDataType.TEXT));
    return new UserDeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private UserDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> addDevice(UUID userId, String deviceId) {
    var futureResponse = new CompletableFuture<Void>();
    exists(DatabaseCell.create(userId)).thenAccept(exists ->
      addDevice(userId, deviceId, exists).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addDevice(UUID userId, String deviceId, boolean exists) {
    if (!exists) {
      return insertDevice(userId, deviceId);
    }
    var futureResponse = new CompletableFuture<Void>();
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      addDevice(userId, deviceId, row).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addDevice(UUID userId, String deviceId, DatabaseRow row) {
    var deviceIds = row.findCell(1).<String>listValue();
    if (deviceIds.contains(deviceId)) {
      return CompletableFuture.completedFuture(null);
    }
    deviceIds.add(deviceId);
    return updateDevices(userId, deviceIds);
  }

  private CompletableFuture<Void> insertDevice(UUID userId, String deviceId) {
    return insert(DatabaseRow.of(userId, Lists.newArrayList(deviceId)));
  }

  public void removeDevice(UUID userId, String deviceId) {
    selectRow(DatabaseCell.create(userId)).thenAccept(row ->
      removeDevice(userId, deviceId, row));
  }

  private void removeDevice(UUID userId, String deviceId, DatabaseRow row) {
    var accountIds = row.findCell(1).<String>listValue();
    if (accountIds.size() == 1) {
      deleteDevices(userId);
      return;
    }
    accountIds.remove(deviceId);
    updateDevices(userId, accountIds);
  }

  public CompletableFuture<Void> updateDevices(UUID userId, List<String> deviceIds) {
    return update(DatabaseCell.create(userId), DatabaseRow.of(userId, deviceIds));
  }

  public void deleteDevices(UUID userId) {
    delete(DatabaseCell.create(userId));
  }

  public CompletableFuture<Boolean> deviceExists(UUID userId) {
    return exists(DatabaseCell.create(userId));
  }

  public CompletableFuture<List<String>> findDevicesIfExists(UUID userId) {
    var futureResponse = new CompletableFuture<List<String>>();
    deviceExists(userId).thenAccept(exists -> findDevicesIfExists(userId, exists)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<List<String>> findDevicesIfExists(
    UUID userId, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return findDevices(userId);
  }

  public CompletableFuture<List<String>> findDevices(UUID userId) {
    return selectRow(DatabaseCell.create(userId))
      .thenApply(row -> row.findCell(1).listValue());
  }

  public CompletableFuture<List<UUID>> findUsersOfDevice(String deviceId) {
    return selectRows("devices CONTAINS '" + deviceId + "'")
      .thenApply(rows -> rows.stream().map(row ->
        row.findCell(0).uuidValue()).toList());
  }
}