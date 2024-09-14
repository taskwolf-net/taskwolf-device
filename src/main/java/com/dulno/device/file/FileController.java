package com.dulno.device.file;

import com.dulno.device.access.DeviceController;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public final class FileController extends DeviceController {
  private FileController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
  }

  @RequestMapping(path = "/device/file/settings/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findFileSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> futureResponse.complete(Map.of("fileStorage",
        device.fileStorage(), "fileInfo", device.fileInfo(), "fileDelete",
        device.fileDelete(), "folderCreate", device.folderCreate(), "folderDelete",
        device.folderDelete())),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  @RequestMapping(path = "/device/file/settings/update/", method = RequestMethod.POST)
  public void updateFileSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId, device ->
      deviceDatabaseTable().updateDeviceFileSettings(device,
        body.getBoolean("fileStorage"), body.getBoolean("fileInfo"),
        body.getBoolean("fileDelete"), body.getBoolean("folderCreate"),
        body.getBoolean("folderDelete")), () -> {});
  }
}
