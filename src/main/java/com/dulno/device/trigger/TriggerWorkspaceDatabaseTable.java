package com.dulno.device.trigger;

import com.dulno.core.database.*;
import com.google.common.collect.Lists;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TriggerWorkspaceDatabaseTable extends DatabaseTable {
  public static TriggerWorkspaceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String tableName
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("workspace", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    return new TriggerWorkspaceDatabaseTable(connection, keyspace, tableName, columns);
  }

  private TriggerWorkspaceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void initialize() {
    createIfNotExists();
    createIndexIfNotExists("trigger");
  }

  public CompletableFuture<Void> insertContent(
    UUID triggerId, String device, UUID workspace
  ) {
    return insert(DatabaseRow.of(device, workspace, triggerId));
  }

  public CompletableFuture<Void> deleteContent(UUID triggerId) {
    return findContent(triggerId).thenAccept(content ->
      delete(DatabaseCondition.of("trigger", triggerId, "device",
        content.findCell(0).stringValue(), "workspace",
        content.findCell(1).uuidValue())));
  }

  public CompletableFuture<DatabaseRow> findContent(UUID triggerId) {
    return selectRow(DatabaseCondition.of("trigger", triggerId));
  }

  public CompletableFuture<List<DatabaseRow>> findContentByCondition(
    DatabaseCondition condition
  ) {
    return selectRows(condition);
  }
}
