package net.taskwolf.device.notification;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
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
public final class NotificationController extends TaskwolfRestController {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final DeviceNotificationDatabaseTable deviceNotificationDatabaseTable;

  private NotificationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    DeviceNotificationDatabaseTable deviceNotificationDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.deviceDatabaseTable = deviceDatabaseTable;
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
    findUser(request).thenApply(user -> deviceDatabaseTable.deviceExists(deviceId)
      .thenAccept(exists -> findNotificationsSettings(user, deviceId, exists)
        .thenAccept(futureResponse::complete)));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findNotificationsSettings(
    User user, String deviceId, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return deviceDatabaseTable.findDevice(deviceId).thenApply(device ->
      findNotificationsSettings(user, device));
  }

  private Map<String, Object> findNotificationsSettings(
    User user, Device device
  ) {
    if (!user.id().equals(device.ownerId())) {
      return Maps.newHashMap();
    }
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
    findUser(request).thenApply(user -> deviceDatabaseTable.deviceExists(deviceId)
      .thenAccept(exists -> updateNotificationsSettings(user, deviceId,
        body.getBoolean("workflowNotifications"), body.getBoolean("errorNotifications"),
        body.getBoolean("newsNotifications"), exists)));
  }

  private void updateNotificationsSettings(
    User user, String deviceId, boolean workflowNotifications,
    boolean errorNotifications, boolean newsNotifications, boolean exists
  ) {
    if (!exists) {
      return;
    }
    deviceDatabaseTable.findDevice(deviceId).thenAccept(device ->
      updateNotificationsSettings(user, device, workflowNotifications,
        errorNotifications, newsNotifications));
  }

  private void updateNotificationsSettings(
    User user, Device device, boolean workflowNotifications,
    boolean errorNotifications, boolean newsNotifications
  ) {
    if (!user.id().equals(device.ownerId())) {
      return;
    }
    deviceDatabaseTable.updateDeviceNotificationSettings(device,
      workflowNotifications, errorNotifications, newsNotifications);
  }

  @RequestMapping(path = "/device/desktop/notifications/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findNotifications(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenApply(user -> deviceDatabaseTable.deviceExists(deviceId)
      .thenAccept(exists -> findNotifications(user, deviceId, exists)
        .thenAccept(futureResponse::complete)));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findNotifications(
    User user, String deviceId, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return deviceDatabaseTable.findDevice(deviceId).thenCompose(device ->
      findNotifications(user, device));
  }

  private CompletableFuture<Map<String, Object>> findNotifications(
    User user, Device device
  ) {
    if (!user.id().equals(device.ownerId())) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
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


