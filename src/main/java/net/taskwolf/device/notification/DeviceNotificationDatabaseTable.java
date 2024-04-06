package net.taskwolf.device.notification;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DeviceNotificationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device_notification";

  public static DeviceNotificationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseListColumn.create("notifications", DatabaseDataType.UUID));
    return new DeviceNotificationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private DeviceNotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> addNotification(
    String deviceId, UUID notificationId
  ) {
    var futureResponse = new CompletableFuture<Void>();
    exists(DatabaseCell.create(deviceId)).thenAccept(exists ->
      addNotification(deviceId, notificationId, exists)
        .thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addNotification(
    String deviceId, UUID notificationId, boolean exists
  ) {
    if (!exists) {
      return insertNotification(deviceId, notificationId);
    }
    var futureResponse = new CompletableFuture<Void>();
    selectRow(DatabaseCell.create(deviceId)).thenAccept(row ->
      addNotification(deviceId, notificationId, row).
        thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<Void> addNotification(
    String deviceId, UUID notificationId, DatabaseRow row
  ) {
    var notificationIds = row.findCell(1).<UUID>listValue();
    if (notificationIds.contains(notificationId)) {
      return CompletableFuture.completedFuture(null);
    }
    notificationIds.add(notificationId);
    return updateNotifications(deviceId, notificationIds);
  }

  private CompletableFuture<Void> insertNotification(
    String deviceId, UUID notificationId
  ) {
    return insert(DatabaseRow.of(deviceId, Lists.newArrayList(notificationId)));
  }

  public void removeNotification(String deviceId, UUID notificationId) {
    selectRow(DatabaseCell.create(deviceId)).thenAccept(row ->
      removeNotification(deviceId, notificationId, row));
  }

  private void removeNotification(
    String deviceId, UUID notificationId, DatabaseRow row
  ) {
    var notificationIds = row.findCell(1).<UUID>listValue();
    if (notificationIds.size() == 1) {
      deleteNotifications(deviceId);
      return;
    }
    notificationIds.remove(notificationId);
    updateNotifications(deviceId, notificationIds);
  }

  public CompletableFuture<Void> updateNotifications(
    String deviceId, List<UUID> notificationIds
  ) {
    return update(DatabaseCell.create(deviceId),
      DatabaseRow.of(deviceId, notificationIds));
  }

  public void deleteNotifications(String deviceId) {
    delete(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<Boolean> notificationExists(String deviceId) {
    return exists(DatabaseCell.create(deviceId));
  }

  public CompletableFuture<List<UUID>> findNotificationsIfExists(String deviceId) {
    var futureResponse = new CompletableFuture<List<UUID>>();
    notificationExists(deviceId).thenAccept(exists ->
      findNotificationsIfExists(deviceId, exists).thenAccept(futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<List<UUID>> findNotificationsIfExists(
    String deviceId, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return findNotifications(deviceId);
  }

  public CompletableFuture<List<UUID>> findNotifications(String deviceId) {
    return selectRow(DatabaseCell.create(deviceId))
      .thenApply(row -> row.findCell(1).listValue());
  }
}
