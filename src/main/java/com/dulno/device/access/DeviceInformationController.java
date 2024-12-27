package com.dulno.device.access;

import com.dulno.core.locale.Translation;
import com.dulno.device.structure.*;
import com.dulno.workflow.WorkflowModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.database.paging.DatabaseDirection;
import com.dulno.core.database.paging.DatabaseOrder;
import com.dulno.core.database.paging.DatabasePage;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.organization.Organization;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class DeviceInformationController extends DeviceController {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final Translation translation;

  private DeviceInformationController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable, Translation translation
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.translation = translation;
  }

  @RequestMapping(path = "/device/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDevice(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request)
      .thenApply(user -> findDeviceTarget(user.id())
        .thenAccept(target -> userDeviceDatabaseTable.userHasDevice(target, deviceId)
          .thenAccept(devices -> findDevice(target, body.getString("device"), devices)
            .thenAccept(futureResponse::complete))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findDevice(
    UUID target, String deviceId, boolean hasAccess
  ) {
    if (!hasAccess) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return deviceDatabaseTable().findDevice(deviceId).thenCompose(device ->
      gatherDeviceInformation(target, device));
  }

  @RequestMapping(path = "/devices/page/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDevicePage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var targetPage = body.getInt("targetPage");
    var sortingColumn = body.getString("sorting");
    var sortingOrder = DatabaseOrder.valueOf(body.getString("order"));
    var search = body.getString("search");
    var ownerId = body.has("owner") ? body.getUUID("owner") : null;
    var platform = body.has("platform") ? body.getString("platform") : null;
    return findUser(request).thenCompose(user -> findDeviceTarget(user.id())
      .thenCompose(target -> userDeviceDatabaseTable.findUserDevices(target,
          targetPage, sortingColumn, sortingOrder, search, ownerId, platform)
        .thenCompose(result -> collectDeviceInformation(user, result))));
  }

  @RequestMapping(path = "/devices/page/shift/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findPreviousDevicePage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var pageState = body.getString("pageState");
    var startingPoint = DatabaseDirection.valueOf(body.getString("startingPoint"));
    var direction = DatabaseDirection.valueOf(body.getString("direction"));
    var sortingColumn = body.getString("sorting");
    var sortingOrder = DatabaseOrder.valueOf(body.getString("order"));
    var ownerId = body.has("owner") ? body.getUUID("owner") : null;
    var platform = body.has("platform") ? body.getString("platform") : null;
    return findUser(request).thenCompose(user -> findDeviceTarget(user.id())
      .thenCompose(target -> userDeviceDatabaseTable.findUserDevices(target,
        pageState, startingPoint, direction, sortingColumn, sortingOrder,
        ownerId, platform))
      .thenCompose(result -> collectDeviceInformation(user, result)));
  }

  private CompletableFuture<Map<String, Object>> collectDeviceInformation(
    User user, DatabasePage<UserDevice> page
  ) {
    if (page.content().isEmpty()) {
      return CompletableFuture.completedFuture(Map.of("devices",
        Lists.newArrayList(), "page", page.pageState(), "pageNumber", 0));
    }
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    AsyncIterator.execute(page.content(),
        device -> gatherDeviceInformation(user.id(), device))
      .thenApply(information -> reconstructDeviceOrder(page, information))
      .thenAccept(information -> futureResponse.complete(Map.of("devices",
        information, "page", page.pageState(), "pageNumber", page.pageNumber())));
    return futureResponse;
  }

  private List<Map<String, Object>> reconstructDeviceOrder(
    DatabasePage<UserDevice> page, List<Map<String, Object>> information
  ) {
    var result = Lists.<Map<String, Object>>newArrayList();
    for (var device : page.content()) {
      for (var entry : information) {
        if (device.deviceId().toString().equals(entry.get("id").toString())) {
          result.add(entry);
          break;
        }
      }
    }
    return result;
  }

  private CompletableFuture<Map<String, Object>> gatherDeviceInformation(
    UUID target, Device device
  ) {
    return gatherDeviceInformation(target, device.id(), device.ownerId(),
      device.information(), device.platform());
  }

  private CompletableFuture<Map<String, Object>> gatherDeviceInformation(
    UUID target, UserDevice device
  ) {
    return gatherDeviceInformation(target, device.deviceId(), device.ownerId(),
      device.information(), device.platform());
  }

  private CompletableFuture<Map<String, Object>> gatherDeviceInformation(
    UUID target, String deviceId, UUID ownerId, String information,
    DevicePlatform platform
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    userDatabaseTable().findUserIfExists(ownerId)
      .thenAccept(owner -> futureResponse.complete(
        assemblyDeviceInformation(target, deviceId, information, platform, owner)));
    return futureResponse;
  }

  private Map<String, Object> assemblyDeviceInformation(
    UUID target, String deviceId, String information, DevicePlatform platform,
    User owner
  ) {
    var result = Maps.<String, Object>newHashMap();
    result.put("id", deviceId);
    result.put("information", information);
    result.put("owner", owner.name());
    result.put("ownDevice", target.equals(owner.id()));
    result.put("platform", platform.toString());
    return result;
  }

  @RequestMapping(path = "/device/users/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDeviceUsers(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    findUser(request).thenAccept(user -> performDeviceOperation(user.id(),
      deviceId, device -> findDeviceUsers(user, device)
        .thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap())));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findDeviceUsers(
    User user, Device device
  ) {
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    userDeviceDatabaseTable.findUsersOfDevice(device.id()).thenApply(users ->
        users.stream().filter(entry -> !entry.equals(user.id())).toList())
      .thenAccept(users -> AsyncIterator.execute(users, target ->
          findUserInformation(user, target))
        .thenAccept(information -> futureResponse.complete(
          Map.of("users", information))));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findUserInformation(
    User user, UUID targetId
  ) {
    return organizationDatabaseTable.organizationExists(targetId)
      .thenCompose(exists -> exists ?
        findUserInformation(targetId, targetId, translation.translate(user,
          "organization.team.target.global")) :
        teamDatabaseTable().findTeam(targetId).thenCompose(team ->
          findUserInformation(team.organizationId(), team.id(), team.name())));
  }

  private CompletableFuture<Map<String, Object>> findUserInformation(
    UUID organizationId, UUID teamId, String teamName
  ) {
    return organizationDatabaseTable.findOrganization(organizationId)
      .thenCompose(organization -> userDatabaseTable().findUser(organization.owner())
        .thenApply(owner -> assemblyUserInformation(organization, owner,
          teamId, teamName)));
  }

  private Map<String, Object> assemblyUserInformation(
    Organization organization, User owner, UUID teamId, String teamName
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("organizationId", organization.id());
    information.put("organizationName", organization.name());
    information.put("organizationOwner", owner.name());
    information.put("teamId", teamId);
    information.put("teamName", teamName);
    return information;
  }

  @RequestMapping(path = "/device/language/find/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findDeviceLanguage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> futureResponse.complete(Map.of("language", device.language())),
        () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }
}

