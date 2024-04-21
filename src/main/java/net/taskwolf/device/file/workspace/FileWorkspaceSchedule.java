package net.taskwolf.device.file.workspace;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.iterator.AsyncListIterator;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(staticName = "create")
public final class FileWorkspaceSchedule {
  private final Distribution distribution;
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> scheduler;

  private static final int WORKSPACE_CHECK_INITIAL_DELAY = 10;
  private static final int WORKSPACE_CHECK_INTERVAL = 10;
  private static final TimeUnit WORKSPACE_CHECK_TIME_UNIT = TimeUnit.SECONDS;

  public void start() {
    scheduler = executorService.scheduleAtFixedRate(this::execute,
      WORKSPACE_CHECK_INITIAL_DELAY, WORKSPACE_CHECK_INTERVAL,
      WORKSPACE_CHECK_TIME_UNIT);
  }

  private void execute() {
    var users = distribution.findAssignedUsers("device");
    AsyncListIterator.execute(users, deviceDatabaseTable::findDevicesOfOwner,
      users.size(), devices -> devices.forEach(this::checkDeviceWorkspace));
  }

  private void checkDeviceWorkspace(Device device) {
    if (device.platform().isDesktop()) {
      return;
    }
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(this::sendRequest);
  }

  private void sendRequest(String identifier) {
    FirebaseRequest.create(deviceConfiguration, identifier).send("data",
      Map.of("workspaceMonitor", true));
  }

  public void stop() {
    scheduler.cancel(false);
  }
}
