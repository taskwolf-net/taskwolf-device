package net.taskwolf.device.file;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.device.file.workspace.FileWorkspaceDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class FileInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  @Named("fileStorageDatabaseTable")
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
  @Named("fileInfoDatabaseTable")
  FileHistoryDatabaseTable provideFileInfoDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_info");
    fileDatabaseTable.createIfNotExists();
    return fileDatabaseTable;
  }

  @Provides
  @Singleton
  @Named("fileDeleteDatabaseTable")
  FileHistoryDatabaseTable provideFileDeleteDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_delete");
    fileDatabaseTable.createIfNotExists();
    return fileDatabaseTable;
  }

  @Provides
  @Singleton
  @Named("fileStorageRequestRepository")
  FileRequestRepository provideFileStorageRequestRepository() {
    return FileRequestRepository.create();
  }

  @Provides
  @Singleton
  @Named("fileInfoRequestRepository")
  FileRequestRepository provideFileInfoRequestRepository() {
    return FileRequestRepository.create();
  }

  @Provides
  @Singleton
  @Named("fileDeleteRequestRepository")
  FileRequestRepository provideFileDeleteRequestRepository() {
    return FileRequestRepository.create();
  }

  @Provides
  @Singleton
  FileWorkspaceDatabaseTable provideFileWorkspaceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workspaceDatabaseTable = FileWorkspaceDatabaseTable.create(
      connection, keyspace);
    workspaceDatabaseTable.createIfNotExists();
    return workspaceDatabaseTable;
  }
}
