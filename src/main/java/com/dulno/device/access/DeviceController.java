package com.dulno.device.access;

import com.dulno.core.access.DulnoRestController;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.organization.team.Team;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;

import java.security.Key;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Accessors(fluent = true)
public class DeviceController extends DulnoRestController {
  @Getter(AccessLevel.PROTECTED)
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;
  private final TeamTargetDatabaseTable teamTargetDatabaseTable;
  @Getter(AccessLevel.PROTECTED)
  private final TeamDatabaseTable teamDatabaseTable;

  protected DeviceController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
    this.teamTargetDatabaseTable = teamTargetDatabaseTable;
    this.teamDatabaseTable = teamDatabaseTable;
  }

  protected void performDeviceOperation(
    UUID userId, String deviceId, Consumer<Device> operation, Runnable failResponse
  ) {
    deviceDatabaseTable.deviceExists(deviceId).thenAccept(exists ->
      performDeviceOperation(userId, deviceId, exists, operation, failResponse));
  }

  private void performDeviceOperation(
    UUID userId, String deviceId, boolean deviceExists,
    Consumer<Device> operation, Runnable failResponse
  ) {
    if (!deviceExists) {
      failResponse.run();
      return;
    }
    deviceDatabaseTable.findDevice(deviceId).thenAccept(device ->
      performDeviceOperation(userId, device, operation, failResponse));
  }

  private void performDeviceOperation(
    UUID userId, Device device, Consumer<Device> operation, Runnable failResponse
  ) {
    if (!device.ownerId().equals(userId)) {
      failResponse.run();
      return;
    }
    operation.accept(device);
  }

  protected void performDeviceOrganizationOperation(
    UUID userId, String deviceId, UUID organizationId, UUID teamId,
    Consumer<UUID> operation, Runnable failResponse
  ) {
    performDeviceOperation(userId, deviceId,
      device -> userDatabaseTable().findUser(userId)
        .thenAccept(user -> performDeviceOrganizationOperation(user,
          organizationId, teamId, operation, failResponse)),
      failResponse);
  }

  private void performDeviceOrganizationOperation(
    User user, UUID organizationId, UUID teamId, Consumer<UUID> operation,
    Runnable failResponse
  ) {
    if (!user.organizations().contains(organizationId)) {
      failResponse.run();
      return;
    }
    if (user.id().equals(organizationId)) {
      failResponse.run();
      return;
    }
    if (organizationId.equals(teamId)) {
      operation.accept(organizationId);
      return;
    }
    teamDatabaseTable.findTeamsByOrganization(organizationId).thenAccept(teams ->
      performDeviceOrganizationOperation(teamId, teams, operation, failResponse));
  }

  private void performDeviceOrganizationOperation(
    UUID teamId, List<Team> teams, Consumer<UUID> operation,
    Runnable failResponse
  ) {
    if (teams.stream().noneMatch(team -> team.id().equals(teamId))) {
      failResponse.run();
      return;
    }
    operation.accept(teamId);
  }

  protected CompletableFuture<UUID> findDeviceTarget(UUID userId) {
    return userTargetDatabaseTable.findTargetSecured(userId).thenCompose(target ->
      userId.equals(target) ? CompletableFuture.completedFuture(target) :
        teamTargetDatabaseTable.findTargetSecured(userId)
          .thenApply(team -> team.orElse(target)));
  }
}