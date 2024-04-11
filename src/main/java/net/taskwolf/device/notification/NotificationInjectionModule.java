package net.taskwolf.device.notification;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class NotificationInjectionModule extends AbstractModule {
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
