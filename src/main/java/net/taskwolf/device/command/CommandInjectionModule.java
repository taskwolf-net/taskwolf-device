package net.taskwolf.device.command;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

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
    return commandExecutionDatabaseTable;
  }
}
