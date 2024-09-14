package com.dulno.device.firebase;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class FirebaseInjectionModule extends AbstractModule {
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
