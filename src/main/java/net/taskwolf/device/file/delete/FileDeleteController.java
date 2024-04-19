package net.taskwolf.device.file.delete;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.access.DeviceController;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileDeleteResponse;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.FileHistoryEntry;
import net.taskwolf.device.file.FilePath;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
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
public final class FileDeleteController extends DeviceController {
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;
  private final DistributionClientRegistry clientRegistry;

  private FileDeleteController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable, @Qualifier("fileDeleteDatabaseTable")
    FileHistoryDatabaseTable fileDeleteDatabaseTable,
    DistributionClientRegistry clientRegistry
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
    this.clientRegistry = clientRegistry;
  }

  @RequestMapping(path = "/device/file/delete/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileDeleteHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> findFileDeleteHistory(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findFileDeleteHistory(
    Device device
  ) {
    return fileDeleteDatabaseTable.findEntriesOfDevice(device.id())
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

  @RequestMapping(path = "/device/file/delete/history/reset/",
    method = RequestMethod.POST)
  public void resetFileDeleteHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      this::resetFileDeleteHistory, () -> {});
  }

  private void resetFileDeleteHistory(Device device) {
    fileDeleteDatabaseTable.findEntriesOfDevice(device.id())
      .thenAccept(entries -> entries.forEach(entry ->
        fileDeleteDatabaseTable.deleteEntry(entry.id())));
  }

  @RequestMapping(path = "/device/file/delete/response/", method = RequestMethod.POST)
  public void deviceFileDeleteResponse(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> deviceFileDeleteResponse(body.getUUID("delete")), () -> {});
  }

  private void deviceFileDeleteResponse(
    UUID deleteId
  ) {
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingFileDeleteResponse(deleteId, true));
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
