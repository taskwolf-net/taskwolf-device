package com.dulno.device.structure;

import com.google.common.collect.Lists;
import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseComparison;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.database.paging.DatabaseDirection;
import com.dulno.core.database.paging.DatabaseOrder;
import com.dulno.core.database.paging.DatabasePage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserDeviceDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "user_device";

  public static UserDeviceDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("target", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("device", DatabaseDataType.TEXT,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("information", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("platform", DatabaseDataType.TEXT));
    var table = new UserDeviceDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("device");
    table.createIndexIfNotExists("information",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.initializeViews();
    return table;
  }

  private DatabaseTable informationView;
  private DatabaseTable ownerView;
  private DatabaseTable platformView;

  private UserDeviceDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    informationView = createMaterializedViewIfNotExists("information_view",
      "information");
    ownerView = createMaterializedViewIfNotExists("owner_view", "owner");
    platformView = createMaterializedViewIfNotExists("platform_view", "platform");
  }

  public CompletableFuture<Void> insertUserDevice(UserDevice device) {
    return insertUserDevice(device.targetId(), device.deviceId(), device.ownerId(),
      device.information(), device.platform());
  }

  public CompletableFuture<Void> insertUserDevice(
    UUID targetId, String deviceId, UUID ownerId, String information,
    DevicePlatform platform
  ) {
    return insert(DatabaseRow.of(targetId, deviceId, ownerId, information,
      platform.toString()));
  }

  public void deleteUserDevice(UUID targetId, String deviceId) {
    delete(DatabaseCondition.of("target", targetId, "device", deviceId));
  }

  public CompletableFuture<Boolean> userDeviceExists(UUID targetId) {
    return exists(DatabaseCondition.of("target", targetId));
  }

  public CompletableFuture<Boolean> userHasDevice(UUID targetId, String deviceId) {
    return exists(DatabaseCondition.of("target", targetId, "device", deviceId));
  }

  private static final int PAGE_SIZE = 5;

  public CompletableFuture<DatabasePage<UserDevice>> findUserDevices(
    UUID targetId, int targetPage, String sortingColumn, DatabaseOrder sortingOrder,
    String search, UUID ownerId, String platform
  ) {
    if (!search.isEmpty()) {
      var condition = DatabaseCondition.of(
        DatabaseComparison.create("target", targetId),
        DatabaseComparison.create("information", "%" + search + "%",
          DatabaseComparison.Type.LIKE));
      return selectRows(condition, PAGE_SIZE)
        .thenApply(rows -> createDevicePage(DatabasePage.create(rows, "", 1), this));
    }
    var view = findTargetView(sortingColumn);
    return view.selectPage(targetId, createDevicesConditions(ownerId, platform),
        sortingOrder, PAGE_SIZE, targetPage)
      .thenApply(page -> createDevicePage(page, view));
  }

  public CompletableFuture<DatabasePage<UserDevice>> findUserDevices(
    UUID targetId, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction, String sortingColumn, DatabaseOrder sortingOrder,
    UUID ownerId, String platform
  ) {
    var view = findTargetView(sortingColumn);
    return view.shiftPage(targetId, createDevicesConditions(ownerId, platform),
        sortingOrder, PAGE_SIZE, pageState, startingPoint, direction)
      .thenApply(page -> createDevicePage(page, view));
  }

  private DatabaseTable findTargetView(String sortingColumn) {
    if (sortingColumn.equals("information")) {
      return informationView;
    } else if (sortingColumn.equals("owner")) {
      return ownerView;
    } else if (sortingColumn.equals("platform")) {
      return platformView;
    }
    return null;
  }

  private DatabaseCondition createDevicesConditions(
    UUID ownerId, String platform
  ) {
    var comparisons = Lists.<DatabaseComparison>newArrayList();
    if (ownerId != null) {
      comparisons.add(DatabaseComparison.create("owner", ownerId));
    }
    if (platform != null) {
      comparisons.add(DatabaseComparison.create("platform", platform));
    }
    return DatabaseCondition.create(comparisons);
  }

  private DatabasePage<UserDevice> createDevicePage(
    DatabasePage<DatabaseRow> page, DatabaseTable table
  ) {
    return DatabasePage.create(
      page.content().stream().map(row -> UserDevice.of(row, table)).toList(),
      page.pageState(), page.pageNumber());
  }

  public CompletableFuture<List<UserDevice>> findAllUserDevices(UUID targetId) {
    return selectRows(DatabaseCondition.of("target", targetId)).thenApply(rows ->
      rows.stream().map(row -> UserDevice.of(row, this)).toList());
  }

  public CompletableFuture<List<UUID>> findUsersOfDevice(String deviceId) {
    return selectRows(DatabaseCondition.of("device", deviceId))
      .thenApply(rows -> rows.stream().map(row ->
        row.findCell(0).uuidValue()).toList());
  }

  public CompletableFuture<List<UserDevice>> findUserDevicesById(String deviceId) {
    return selectRows(DatabaseCondition.of("device", deviceId)).thenApply(rows ->
      rows.stream().map(row -> UserDevice.of(row, this)).toList());
  }
}