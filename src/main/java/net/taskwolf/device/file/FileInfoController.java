package net.taskwolf.device.file;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.access.DeviceController;
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
import java.util.concurrent.CompletableFuture;

@RestController
public final class FileInfoController extends DeviceController {
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;

  private FileInfoController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable, @Qualifier("fileInfoDatabaseTable")
    FileHistoryDatabaseTable fileInfoDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
  }

  @RequestMapping(path = "/device/file/info/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileInfoHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
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
    information.put("executed", formatTime(entry.executed()));
    return information;
  }

  @RequestMapping(path = "/device/file/info/history/reset/",
    method = RequestMethod.POST)
  public void resetFileInfoHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      this::resetFileInfoHistory, () -> {});
  }

  private void resetFileInfoHistory(Device device) {
    fileInfoDatabaseTable.findEntriesOfDevice(device.id())
      .thenAccept(entries -> entries.forEach(entry ->
        fileInfoDatabaseTable.deleteEntry(entry.id())));
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
