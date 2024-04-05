package net.taskwolf.device.notification;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NotificationDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "notification";

  public static NotificationDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("title", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("body", DatabaseDataType.TEXT));
    return new NotificationDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private NotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertNotification(NotificationEntry notification) {
    insertNotification(notification.id(), notification.title(), notification.body());
  }

  public void insertNotification(
    UUID id, String title, String body
  ) {
    insert(DatabaseRow.of(id, title, body));
  }

  public void deleteNotification(UUID notificationId) {
    delete(DatabaseCell.create(notificationId));
  }

  public CompletableFuture<UUID> generateAvailableNotificationId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    notificationExists(id).thenApply(exists -> exists ?
      generateAvailableNotificationId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> notificationExists(UUID notificationId) {
    return exists(DatabaseCell.create(notificationId));
  }


  public CompletableFuture<NotificationEntry> findNotification(UUID notificationId) {
    return selectRow(DatabaseCell.create(notificationId))
      .thenApply(NotificationEntry::of);
  }
}
