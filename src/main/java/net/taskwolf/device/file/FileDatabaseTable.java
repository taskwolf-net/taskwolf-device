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
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("executed", DatabaseDataType.BIGINT));
    return new FileDatabaseTable(connection, keyspace, tableName, columns);
  }

  private FileDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertFile(FileEntry file) {
    insertFile(file.id(), file.device(), file.path(), file.executed());
  }

  public void insertFile(
    UUID id, String device, String path, long executed
  ) {
    insert(DatabaseRow.of(id, device, path, executed));
  }

  public void deleteFile(UUID fileId) {
    delete(DatabaseCell.create(fileId));
  }

  public CompletableFuture<UUID> generateAvailableFileId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    fileExists(id).thenApply(exists -> exists ?
      generateAvailableFileId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> fileExists(UUID fileId) {
    return exists(DatabaseCell.create(fileId));
  }

  public CompletableFuture<FileEntry> findFile(UUID fileId) {
    return selectRow(DatabaseCell.create(fileId))
      .thenApply(FileEntry::of);
  }

  public CompletableFuture<List<FileEntry>> findFilesOfDevice(
    String deviceId
  ) {
    return selectRows("device='" + deviceId + "' ALLOW FILTERING")
      .thenApply(rows -> rows.stream().map(FileEntry::of).toList());
  }
}

