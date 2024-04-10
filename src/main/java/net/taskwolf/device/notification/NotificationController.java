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
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final DeviceNotificationDatabaseTable deviceNotificationDatabaseTable;

  private NotificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    DeviceNotificationDatabaseTable deviceNotificationDatabaseTable
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable);
    this.notificationDatabaseTable = notificationDatabaseTable;
    this.deviceNotificationDatabaseTable = deviceNotificationDatabaseTable;
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

  @RequestMapping(path = "/device/desktop/notifications/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findNotifications(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> findNotifications(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findNotifications(
    Device device
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    var futureNotifications =
      deviceNotificationDatabaseTable.findNotificationsIfExists(device.id());
    futureNotifications.thenAccept(notificationIds -> AsyncIterator.execute(
      notificationIds, notificationDatabaseTable::findNotification,
      notificationIds.size(), notifications -> futureResponse.complete(
        completeNotificationFinding(device.id(), notifications))));
    return futureResponse;
  }

  private Map<String, Object> completeNotificationFinding(
    String deviceId, List<NotificationEntry> notifications
  ) {
    for (var notification : notifications) {
      notificationDatabaseTable.deleteNotification(notification.id());
    }
    deviceNotificationDatabaseTable.deleteNotifications(deviceId);
    return Map.of("notifications", notifications.stream()
      .map(this::assemblyNotificationInformation).toList());
  }

  private Map<String, Object> assemblyNotificationInformation(
    NotificationEntry notification
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("title", notification.title());
    information.put("body", notification.body());
    return information;
  }
}


