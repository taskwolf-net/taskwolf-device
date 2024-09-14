package com.dulno.device.command;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class CommandInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  CommandExecutionDatabaseTable provideCommandExecutionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var commandExecutionDatabaseTable = CommandExecutionDatabaseTable.create(
      connection, keyspace);
    commandExecutionDatabaseTable.createIfNotExists();
    commandExecutionDatabaseTable.createIndexIfNotExists("device");
    return commandExecutionDatabaseTable;
  }
}
