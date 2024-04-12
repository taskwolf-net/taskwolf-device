package net.taskwolf.device.firebase;

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
    columns.add(DatabaseColumn.create("identifier", DatabaseDataType.TEXT));
    return new FirebaseDeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private FirebaseDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void storeDeviceIdentifier(String deviceId, String identifier) {
    exists(DatabaseCell.create(deviceId)).thenAccept(exists ->
      storeDeviceIdentifier(deviceId, identifier, exists));
  }

  private void storeDeviceIdentifier(String deviceId, String identifier, boolean exists) {
    if (!exists) {
      insertDeviceIdentifier(deviceId, identifier);
    } else {
      updateDeviceIdentifier(deviceId, identifier);
    }
  }

  private CompletableFuture<Void> insertDeviceIdentifier(String deviceId, String identifier) {
    return insert(DatabaseRow.of(deviceId, identifier));
  }

  private CompletableFuture<Void> updateDeviceIdentifier(String deviceId, String identifier) {
    return update(DatabaseCell.create(deviceId), DatabaseRow.of(deviceId, identifier));
  }

  public void deleteDeviceIdentifier(String deviceId) {
    delete(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<Boolean> deviceIdentifierExists(String deviceId) {
    return exists(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<String> findDeviceIdentifier(String deviceId) {
    return selectRow(DatabaseCell.create(deviceId))
      .thenApply(row -> row.findCell(1).stringValue());
  }
}
