package net.taskwolf.device.file;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class FileInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  FileHistoryDatabaseTable provideFileStorageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_storage");
    fileDatabaseTable.createIfNotExists();
    return fileDatabaseTable;
  }

  @Provides
  @Singleton
  FileHistoryDatabaseTable provideFileInfoDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_info");
    fileDatabaseTable.createIfNotExists();
    return fileDatabaseTable;
  }
}
