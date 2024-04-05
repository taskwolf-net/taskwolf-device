package net.taskwolf.device.notification;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "creaet")
public final class NotificationInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  NotificationDatabaseTable provideNotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var notificationDatabaseTable = NotificationDatabaseTable.create(connection,
      keyspace);
    notificationDatabaseTable.createIfNotExists();
    return notificationDatabaseTable;
  }

  @Provides
  @Singleton
  DeviceNotificationDatabaseTable provideDeviceNotificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var deviceNotificationDatabaseTable = DeviceNotificationDatabaseTable.create(
      connection, keyspace);
    deviceNotificationDatabaseTable.createIfNotExists();
    return deviceNotificationDatabaseTable;
  }

  @Provides
  @Singleton
  FirebaseDeviceDatabaseTable provideFirebaseDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var firebaseDeviceDatabaseTable = FirebaseDeviceDatabaseTable.create(
      connection, keyspace);
    firebaseDeviceDatabaseTable.createIfNotExists();
    return firebaseDeviceDatabaseTable;
  }
}
