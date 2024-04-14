package net.taskwolf.device.file;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class FileDatabaseTable extends DatabaseTable {
  public static FileDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String tableName
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.BLOB));
    return new FileDatabaseTable(connection, keyspace, tableName, columns);
  }

  private FileDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertEntry(UUID id, byte[] content) {
    insert(DatabaseRow.of(id, content));
  }

  public void deleteEntry(UUID entryId) {
    delete(DatabaseCell.create(entryId));
  }

  public CompletableFuture<Boolean> entryExists(UUID entryId) {
    return exists(DatabaseCell.create(entryId));
  }

  public CompletableFuture<byte[]> findEntry(UUID entryId) {
    return selectRow(DatabaseCell.create(entryId))
      .thenApply(row -> (byte[]) row.findCell(1).rawValue());
  }
}
