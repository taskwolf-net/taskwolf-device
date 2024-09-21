package com.dulno.device.firebase;

import com.dulno.device.DeviceConfiguration;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

import java.io.FileInputStream;

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

  @Provides
  @Singleton
  GoogleCredentials provideGoogleCredentials(
    DeviceConfiguration configuration
  ) throws Exception {
    return ServiceAccountCredentials
      .fromStream(new FileInputStream(System.getProperty("user.dir") +
        "/configurations/device/" + configuration.firebaseConfigurationName()))
      .createScoped("https://www.googleapis.com/auth/firebase.messaging");
  }
}
