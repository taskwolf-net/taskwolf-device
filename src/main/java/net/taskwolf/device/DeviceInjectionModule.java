package net.taskwolf.device;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.device.command.CommandInjectionModule;
import net.taskwolf.device.file.FileInjectionModule;
import net.taskwolf.device.firebase.FirebaseInjectionModule;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;

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
    var deviceDatabaseTable = DeviceDatabaseTable.create(connection, keyspace);
    deviceDatabaseTable.createIfNotExists();
    deviceDatabaseTable.createIndexIfNotExists("machine");
    deviceDatabaseTable.createIndexIfNotExists("owner");
    return deviceDatabaseTable;
  }

  @Provides
  @Singleton
  UserDeviceDatabaseTable provideUserDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userDeviceDatabaseTable = UserDeviceDatabaseTable.create(connection,
      keyspace);
    userDeviceDatabaseTable.createIfNotExists();
    userDeviceDatabaseTable.createIndexIfNotExists("devices");
    return userDeviceDatabaseTable;
  }
}
