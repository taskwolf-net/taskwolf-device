package net.taskwolf.device.file;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class FileHistoryDatabaseTable extends DatabaseTable {
  public static FileHistoryDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String tableName
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("executed", DatabaseDataType.BIGINT));
    return new FileHistoryDatabaseTable(connection, keyspace, tableName, columns);
  }

  private FileHistoryDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertEntry(FileHistoryEntry entry) {
    insertEntry(entry.id(), entry.device(), entry.path(), entry.name(),
      entry.executed());
  }

  public void insertEntry(
    UUID id, String device, String path, String name, long executed
  ) {
    insert(DatabaseRow.of(id, device, path, name, executed));
  }

  public void deleteEntry(UUID entryId) {
    delete(DatabaseCell.create(entryId));
  }

  public CompletableFuture<Boolean> entryExists(UUID entryId) {
    return exists(DatabaseCell.create(entryId));
  }

  public CompletableFuture<FileHistoryEntry> findEntry(UUID entryId) {
    return selectRow(DatabaseCell.create(entryId))
      .thenApply(FileHistoryEntry::of);
  }

  public CompletableFuture<List<FileHistoryEntry>> findEntriesOfDevice(
    String deviceId
  ) {
    return selectRows("device='" + deviceId + "' ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(FileHistoryEntry::of).toList());
  }
}

