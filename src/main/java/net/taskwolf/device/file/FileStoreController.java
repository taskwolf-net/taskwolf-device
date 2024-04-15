package net.taskwolf.device.file;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.access.DeviceController;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageResponse;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
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
  private final DistributionClientRegistry clientRegistry;
  private final FileStorageRepository fileStorageRepository;

  private FileStoreController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable, @Qualifier("fileStorageDatabaseTable")
    FileHistoryDatabaseTable fileStorageDatabaseTable,
    DistributionClientRegistry clientRegistry,
    FileStorageRepository fileStorageRepository
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.clientRegistry = clientRegistry;
    this.fileStorageRepository = fileStorageRepository;
  }

  @RequestMapping(path = "/device/file/storage/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileStorageHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
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
    information.put("executed", formatTime(entry.executed()));
    return information;
  }

  @RequestMapping(path = "/device/file/storage/history/reset/",
    method = RequestMethod.POST)
  public void resetFileStorageHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
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
    var body = TaskwolfRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> futureResponse.complete(deviceFileStorageResponse(
        body.getUUID("storage"))),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private Map<String, Object> deviceFileStorageResponse(
    UUID storageId
  ) {
    var content = fileStorageRepository.findFileContent(storageId);
    if (content.isEmpty()) {
      return Maps.newHashMap();
    }
    var result = Map.<String, Object>of("content",
      Base64.encodeBase64String(content.get()));
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingFileStorageResponse(storageId, true));
    fileStorageRepository.unregisterFileContent(storageId);
    return result;
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
