package net.taskwolf.device;

import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.DeviceScanDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.device.command.CommandInjectionModule;
import net.taskwolf.device.file.FileInjectionModule;
import net.taskwolf.device.firebase.FirebaseInjectionModule;

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
