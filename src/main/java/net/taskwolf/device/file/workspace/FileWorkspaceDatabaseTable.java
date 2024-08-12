package net.taskwolf.device.file.workspace;

import com.google.common.collect.Lists;
import net.taskwolf.core.database.*;
import net.taskwolf.device.file.FileHistoryEntry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class FileWorkspaceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device_file_workspace";

  public static FileWorkspaceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("path", DatabaseDataType.TEXT));
    return new FileWorkspaceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private FileWorkspaceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertWorkspace(FileHistoryEntry entry) {
    insertWorkspace(entry.id(), entry.device(), entry.path());
  }

  public void insertWorkspace(UUID id, String device, String path) {
    insert(DatabaseRow.of(id, device, path));
  }

  public void deleteWorkspace(UUID workspaceId) {
    delete(DatabaseCell.create(workspaceId));
  }

  public CompletableFuture<UUID> generateAvailableWorkspaceId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    workspaceExists(id).thenApply(exists -> exists ?
      generateAvailableWorkspaceId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> workspaceExists(UUID workspaceId) {
    return exists(DatabaseCell.create(workspaceId));
  }

  public CompletableFuture<Boolean> workspaceExists(String deviceId, String path) {
    return exists("device='" + deviceId + "' AND path='" + path + "'");
  }

  public CompletableFuture<FileWorkspace> findWorkspace(UUID workspaceId) {
    return selectRow(DatabaseCell.create(workspaceId))
      .thenApply(FileWorkspace::of);
  }

  public CompletableFuture<List<FileWorkspace>> findWorkspacesOfDevice(
    String deviceId
  ) {
    return selectRows("device='" + deviceId + "'")
      .thenApply(rows -> rows.stream().map(FileWorkspace::of).toList());
  }
}
