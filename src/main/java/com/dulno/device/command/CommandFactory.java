package com.dulno.device.command;

import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.DeviceConfiguration;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;

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
