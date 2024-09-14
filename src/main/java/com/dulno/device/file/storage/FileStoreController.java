package com.dulno.device.file.storage;

import com.dulno.device.access.DeviceController;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageRedirectRequest;
import com.dulno.device.file.FileHistoryDatabaseTable;
import com.dulno.device.file.FileHistoryEntry;
import com.dulno.device.file.FilePath;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageResponse;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class FileStoreController extends DeviceController {
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final WorkerProxyClient workerProxyClient;
  private final FileStorageRepository fileStorageRepository;
  private final FileStorageRedirectRepository fileStorageRedirectRepository;

  private FileStoreController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    @Qualifier("fileStorageDatabaseTable")
    FileHistoryDatabaseTable fileStorageDatabaseTable,
    WorkerProxyClient workerProxyClient, FileStorageRepository fileStorageRepository,
    FileStorageRedirectRepository fileStorageRedirectRepository
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.workerProxyClient = workerProxyClient;
    this.fileStorageRepository = fileStorageRepository;
    this.fileStorageRedirectRepository = fileStorageRedirectRepository;
  }

  @RequestMapping(path = "/device/file/storage/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileStorageHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> findFileStorageHistory(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findFileStorageHistory(
    Device device
  ) {
    return fileStorageDatabaseTable.findEntriesOfDevice(device.id())
      .thenApply(entries -> entries.stream().sorted(
        Comparator.comparing(FileHistoryEntry::executed).reversed()).toList())
      .thenApply(entries -> Map.of("history", entries.stream()
        .map(this::assemblyEntryInformation).toList()));
  }

  private Map<String, Object> assemblyEntryInformation(
    FileHistoryEntry entry
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("path", entry.path());
    information.put("name", entry.name());
    information.put("compoundPath", FilePath.of(entry.path(), entry.name()).compound());
    information.put("executed", formatTime(entry.executed()));
    return information;
  }

  @RequestMapping(path = "/device/file/storage/history/reset/",
    method = RequestMethod.POST)
  public void resetFileStorageHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      this::resetFileStorageHistory, () -> {});
  }

  private void resetFileStorageHistory(Device device) {
    fileStorageDatabaseTable.findEntriesOfDevice(device.id())
      .thenAccept(entries -> entries.forEach(entry ->
        fileStorageDatabaseTable.deleteEntry(entry.id())));
  }

  @RequestMapping(path = "/device/file/storage/response/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> deviceFileStorageResponse(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      device -> deviceFileStorageResponse(deviceId, body.getUUID("storage"),
        findApiKey(request)).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> deviceFileStorageResponse(
    String deviceId, UUID storageId, String apiKey
  ) {
    var content = fileStorageRepository.findFileContent(storageId);
    if (content.isEmpty()) {
      var futureResponse = new CompletableFuture<Map<String, Object>>();
      fileStorageRedirectRepository.registerStorageRedirect(deviceId, storageId,
        apiKey, futureResponse);
      workerProxyClient.sendPacket(
        new PacketOutgoingFileStorageRedirectRequest(storageId));
      return futureResponse;
    }
    var result = Map.<String, Object>of("content",
      Base64.encodeBase64String(content.get()));
    workerProxyClient.sendPacket(new PacketOutgoingFileStorageResponse(storageId,
      true));
    fileStorageRepository.unregisterFileContent(storageId);
    return CompletableFuture.completedFuture(result);
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
