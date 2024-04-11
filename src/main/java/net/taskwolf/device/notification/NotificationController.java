package net.taskwolf.device.notification;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.device.access.DeviceController;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public final class NotificationController extends DeviceController {
  private NotificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
  }

  @RequestMapping(path = "/device/notification/settings/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findNotificationsSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> futureResponse.complete(findNotificationsSettings(device)),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private Map<String, Object> findNotificationsSettings(
    Device device
  ) {
    return Map.of("workflowNotifications", device.workflowNotifications(),
      "errorNotifications", device.errorNotifications(), "newsNotifications",
      device.newsNotifications());
  }

  @RequestMapping(path = "/device/notification/settings/update/", method = RequestMethod.POST)
  public void updateNotificationsSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      device -> deviceDatabaseTable().updateDeviceNotificationSettings(device,
        body.getBoolean("workflowNotifications"),
        body.getBoolean("errorNotifications"), body.getBoolean("newsNotifications")),
      () -> {});
  }
}


