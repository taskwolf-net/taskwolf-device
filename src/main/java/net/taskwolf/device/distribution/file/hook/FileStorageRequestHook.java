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
import net.taskwolf.device.file.FileStorageRepository;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
      "filePath", event.filePath(), "fileName", event.fileName()));
  }

  private void storeMobileFile(String deviceId, Map<String, Object> data) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(deviceId)
      .thenAccept(identifier -> storeMobileFile(data, identifier));
  }

  private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

  private void storeMobileFile(Map<String, Object> data, String identifier) {
    var requestBody = new JSONObject(Map.of("to", identifier, "data", data));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "key=" + deviceConfiguration.firebaseToken())
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }
}

