package com.dulno.device.file.delete;

import com.dulno.device.access.DeviceController;
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
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileDeleteResponse;
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
  private final WorkerProxyClient workerProxyClient;

  private FileDeleteController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    @Qualifier("fileDeleteDatabaseTable")
    FileHistoryDatabaseTable fileDeleteDatabaseTable,
    WorkerProxyClient workerProxyClient
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
    this.workerProxyClient = workerProxyClient;
  }

  @RequestMapping(path = "/device/file/delete/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileDeleteHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
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
    var body = DulnoRequestBody.of(payload, response);
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
    var body = DulnoRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> deviceFileDeleteResponse(body.getUUID("delete")), () -> {});
  }

  private void deviceFileDeleteResponse(
    UUID deleteId
  ) {
    workerProxyClient.sendPacket(new PacketOutgoingFileDeleteResponse(deleteId,
      true));
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
