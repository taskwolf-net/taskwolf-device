package com.dulno.device.command;

import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class CommandExecutionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "device_command_execution";

  public static CommandExecutionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("created", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("command", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("output", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("errorMessage", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("exitCode", DatabaseDataType.INT));
    return new CommandExecutionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private CommandExecutionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public void insertCommandExecution(CommandExecution execution) {
    insertCommandExecution(execution.id(), execution.device(),
      execution.created(), execution.command(), execution.output(),
      execution.errorMessage(), execution.exitCode());
  }

  public void insertCommandExecution(
    UUID id, String device, long created, String command, String output,
    String errorMessage, int exitCode
  ) {
    insert(DatabaseRow.of(id, device, created, command, output, errorMessage,
      exitCode));
  }

  public void deleteCommandExecution(UUID executionId) {
    delete(executionId);
  }

  public CompletableFuture<UUID> generateAvailableExecutionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    executionExists(id).thenApply(exists -> exists ?
      generateAvailableExecutionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> executionExists(UUID executionId) {
    return exists(executionId);
  }

  public CompletableFuture<CommandExecution> findExecution(UUID executionId) {
    return selectRow(executionId).thenApply(CommandExecution::of);
  }

  public CompletableFuture<List<CommandExecution>> findExecutionsOfDevice(
    String deviceId
  ) {
    return selectRows(DatabaseCondition.of("device", deviceId))
      .thenApply(rows -> rows.stream().map(CommandExecution::of).toList());
  }
}

