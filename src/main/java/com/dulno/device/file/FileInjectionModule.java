package com.dulno.device.file;

import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class FileInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  @Named("fileStorageDatabaseTable")
  FileHistoryDatabaseTable provideFileStorageDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileHistoryDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_storage");
    fileHistoryDatabaseTable.createIfNotExists();
    fileHistoryDatabaseTable.createIndexIfNotExists("device");
    return fileHistoryDatabaseTable;
  }

  @Provides
  @Singleton
  @Named("fileInfoDatabaseTable")
  FileHistoryDatabaseTable provideFileInfoDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileHistoryDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_info");
    fileHistoryDatabaseTable.createIfNotExists();
    fileHistoryDatabaseTable.createIndexIfNotExists("device");
    return fileHistoryDatabaseTable;
  }

  @Provides
  @Singleton
  @Named("fileDeleteDatabaseTable")
  FileHistoryDatabaseTable provideFileDeleteDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var fileHistoryDatabaseTable = FileHistoryDatabaseTable.create(
      connection, keyspace, "device_file_delete");
    fileHistoryDatabaseTable.createIfNotExists();
    fileHistoryDatabaseTable.createIndexIfNotExists("device");
    return fileHistoryDatabaseTable;
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
    workspaceDatabaseTable.createIndexIfNotExists("device");
    workspaceDatabaseTable.createIndexIfNotExists("path");
    return workspaceDatabaseTable;
  }
}
