package net.taskwolf.device.command;

import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.command.packet.outgoing.PacketOutgoingCommandRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Command {
  private final CommandRequestRepository commandRequestRepository;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final WorkerProxyClient workerProxyClient;
  private final Device device;
  private final String command;

  public void execute(CompletableFuture<ActionResult> futureResponse) {
    commandExecutionDatabaseTable.generateAvailableExecutionId().thenAccept(id ->
      execute(futureResponse, id));
  }

  private void execute(
    CompletableFuture<ActionResult> futureResponse, UUID commandId
  ) {
    commandRequestRepository.registerCommandRequest(CommandRequest.create(
      commandId, device, command, futureResponse));
    workerProxyClient.sendPacket(new PacketOutgoingCommandRequest(commandId,
      device.id(), device.platform(), command));
    if (device.platform().isMobile()) {
      executeMobileCommand(commandId);
    }
  }

  private void executeMobileCommand(UUID commandId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAcceptAsync(identifier -> executeMobileCommand(commandId, identifier));
  }

  private void executeMobileCommand(UUID commandId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, googleCredentials, identifier)
      .send("data", Map.of("commandId", commandId, "command", command));
  }
}
