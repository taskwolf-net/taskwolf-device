package com.dulno.device;

import com.dulno.device.structure.DeviceDatabaseTable;
import com.dulno.device.structure.DeviceScanDatabaseTable;
import com.dulno.device.structure.UserDeviceDatabaseTable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.device.command.CommandInjectionModule;
import com.dulno.device.file.FileInjectionModule;
import com.dulno.device.firebase.FirebaseInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(FirebaseInjectionModule.create());
    install(CommandInjectionModule.create());
    install(FileInjectionModule.create());
  }

  @Provides
  @Singleton
  DeviceConfiguration provideDeviceConfiguration() throws Exception {
    return DeviceConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  DeviceDatabaseTable provideDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return DeviceDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  UserDeviceDatabaseTable provideUserDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return UserDeviceDatabaseTable.create(connection,
      keyspace);
  }

  @Provides
  @Singleton
  DeviceScanDatabaseTable provideDeviceScanDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return DeviceScanDatabaseTable.create(connection, keyspace);
  }
}
