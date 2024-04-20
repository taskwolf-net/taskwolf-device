package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.connection.DeviceConnection;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.distribution.file.event.FileStorageRequestEvent;
import net.taskwolf.device.file.FilePath;
import net.taskwolf.device.file.storage.FileStorageRepository;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final FileStorageRepository fileStorageRepository;

  @EventHook
  private void fileStorageRequest(FileStorageRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    deviceDatabaseTable.findDevice(event.deviceId()).thenAccept(device ->
      fileStorageRequest(event, connection.get(), device));
  }

  private void fileStorageRequest(
    FileStorageRequestEvent event, DeviceConnection connection, Device device
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
      .thenAccept(identifier -> FirebaseRequest.create(deviceConfiguration,
        identifier).send("data", data));
  }
}

