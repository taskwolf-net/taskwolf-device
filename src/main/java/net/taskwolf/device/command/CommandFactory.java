package net.taskwolf.device.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandFactory {
  private final CommandRequestRepository commandRequestRepository;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final WorkerProxyClient workerProxyClient;

  public Command createCommand(Device device, String command) {
    return Command.create(commandRequestRepository, commandExecutionDatabaseTable,
      firebaseDeviceDatabaseTable, deviceConfiguration, workerProxyClient, device,
      command);
  }
}
