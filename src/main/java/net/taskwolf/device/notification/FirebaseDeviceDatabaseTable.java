package net.taskwolf.device.notification;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FirebaseDeviceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "firebase_device";

  public static FirebaseDeviceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("token", DatabaseDataType.TEXT));
    return new FirebaseDeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private FirebaseDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void storeDeviceToken(String deviceId, String token) {
    exists(DatabaseCell.create(deviceId)).thenAccept(exists ->
      storeDeviceToken(deviceId, token, exists));
  }

  private void storeDeviceToken(String deviceId, String token, boolean exists) {
    if (!exists) {
      insertDeviceToken(deviceId, token);
    } else {
      updateDeviceToken(deviceId, token);
    }
  }

  private CompletableFuture<Void> insertDeviceToken(String deviceId, String token) {
    return insert(DatabaseRow.of(deviceId, token));
  }

  private CompletableFuture<Void> updateDeviceToken(String deviceId, String token) {
    return update(DatabaseCell.create(deviceId), DatabaseRow.of(deviceId, token));
  }

  public void deleteDeviceToken(String deviceId) {
    delete(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<Boolean> deviceTokenExists(String deviceId) {
    return exists(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<String> findDeviceToken(String deviceId) {
    return selectRow(DatabaseCell.create(deviceId))
      .thenApply(row -> row.findCell(1).stringValue());
  }
}
