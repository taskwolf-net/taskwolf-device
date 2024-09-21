package com.dulno.device.distribution.file.hook;

import com.dulno.device.file.FilePath;
import com.dulno.device.firebase.FirebaseRequest;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.DeviceConfiguration;
import com.dulno.device.connection.DeviceConnection;
import com.dulno.device.connection.DeviceConnectionRepository;
import com.dulno.device.distribution.file.event.WorkerFileStorageRequestEvent;
import com.dulno.device.file.storage.FileStorageRepository;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final FileStorageRepository fileStorageRepository;

  @EventHook
  private void fileStorageRequest(WorkerFileStorageRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    deviceDatabaseTable.findDevice(event.deviceId()).thenAccept(device ->
      fileStorageRequest(event, connection.get(), device));
  }

  private void fileStorageRequest(
    WorkerFileStorageRequestEvent event, DeviceConnection connection, Device device
  ) {
    fileStorageRepository.registerFileContent(event.storeId(), event.content());
    if (device.platform().isDesktop()) {
      connection.storeFile(event.storeId(), event.filePath(), event.fileName());
      return;
    }
    storeMobileFile(event.deviceId(), Map.of("storeId", event.storeId(),
      "filePath", FilePath.of(event.filePath(), event.fileName()).compound()));
  }

  private void storeMobileFile(String deviceId, Map<String, Object> data) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(deviceId)
      .thenAcceptAsync(identifier -> FirebaseRequest.create(deviceConfiguration,
        googleCredentials, identifier).send("data", data));
  }
}

