package net.taskwolf.device.command;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.command.packet.outgoing.PacketOutgoingCommandRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Command {
  private final CommandRequestRepository commandRequestRepository;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;
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
    if (device.platform().isMobile()) {
      executeMobileCommand(commandId);
    } else {
      executeDesktopCommand(commandId);
    }
  }

  private void executeMobileCommand(UUID commandId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(identifier -> executeMobileCommand(commandId, identifier));
  }

  private void executeMobileCommand(UUID commandId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, identifier).send("data",
      Map.of("commandId", commandId, "command", command));
  }

  private void executeDesktopCommand(UUID commandId) {
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst()
      .get().sendPacket(new PacketOutgoingCommandRequest(commandId,
        device.id(), command));
  }
}
