package com.dulno.device.file.info;

import com.dulno.workflow.action.ActionResult;
import com.dulno.device.access.DeviceController;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileInfoResponse;
import com.dulno.device.file.*;
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
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileInfoRedirectRequest;
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
public final class FileInfoController extends DeviceController {
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileRequestRepository fileInfoRepository;
  private final FileInfoRedirectRepository fileInfoRedirectRepository;
  private final WorkerProxyClient workerProxyClient;

  private FileInfoController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    @Qualifier("fileInfoDatabaseTable")
    FileHistoryDatabaseTable fileInfoDatabaseTable,
    @Qualifier("fileInfoRequestRepository") FileRequestRepository fileInfoRepository,
    FileInfoRedirectRepository fileInfoRedirectRepository,
    WorkerProxyClient workerProxyClient
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileInfoRepository = fileInfoRepository;
    this.fileInfoRedirectRepository = fileInfoRedirectRepository;
    this.workerProxyClient = workerProxyClient;
  }

  @RequestMapping(path = "/device/file/info/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileInfoHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> findFileInfoHistory(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findFileInfoHistory(
    Device device
  ) {
    return fileInfoDatabaseTable.findEntriesOfDevice(device.id())
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

  @RequestMapping(path = "/device/file/info/history/reset/",
    method = RequestMethod.POST)
  public void resetFileInfoHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      this::resetFileInfoHistory, () -> {});
  }

  private void resetFileInfoHistory(Device device) {
    fileInfoDatabaseTable.findEntriesOfDevice(device.id())
      .thenAccept(entries -> entries.forEach(entry ->
        fileInfoDatabaseTable.deleteEntry(entry.id())));
  }

  @RequestMapping(path = "/device/file/info/response/", method = RequestMethod.POST)
  public void deviceFileInfoResponse(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var apiKey = findApiKey(request);
    performDeviceOperation(findUserId(request), deviceId,
      device -> deviceFileInfoResponse(deviceId, body.getUUID("info"),
        apiKey, body.getString("content")), () -> {});
  }

  private void deviceFileInfoResponse(
    String deviceId, UUID infoId, String apiKey, String content
  ) {
    var request = fileInfoRepository.findFileRequest(infoId);
    if (request.isEmpty()) {
      fileInfoRedirectRepository.registerInfoRedirect(deviceId, infoId,
        apiKey, content);
      workerProxyClient.sendPacket(
        new PacketOutgoingFileInfoRedirectRequest(infoId));
      return;
    }
    workerProxyClient.sendPacket(new PacketOutgoingFileInfoResponse(infoId, true));
    completeInfoRequest(request.get(), content);
  }

  private void completeInfoRequest(FileRequest request, String content) {
    long time = System.currentTimeMillis();
    request.futureResult().complete(ActionResult.success(buildInformation(
      request.device(), request.path(), request.name(), content)));
    fileInfoRepository.unregisterFileRequest(request);
    fileInfoDatabaseTable.insertEntry(request.id(),
      request.device().id(), request.path(), request.name(), time);
  }

  private Map<String, Object> buildInformation(
    Device device, String filePath, String fileName, String fileContent
  ) {
    var information = device.composition();
    information.put("filePath", filePath);
    information.put("fileName", fileName);
    information.put("fileContent", fileContent);
    return information;
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
