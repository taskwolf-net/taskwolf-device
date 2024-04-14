package net.taskwolf.device.command;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.command.packet.outgoing.PacketOutgoingCommandRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

  private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

  private void executeMobileCommand(UUID commandId, String identifier) {
    var requestBody = new JSONObject(Map.of("to", identifier, "data",
      Map.of("commandId", commandId, "command", command)));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "key=" + deviceConfiguration.firebaseToken())
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }

  private void executeDesktopCommand(UUID commandId) {
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst()
      .get().sendPacket(new PacketOutgoingCommandRequest(commandId,
        device.id(), command));
  }
}
