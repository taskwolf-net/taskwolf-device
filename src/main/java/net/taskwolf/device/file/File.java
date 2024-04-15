package net.taskwolf.device.file;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileInfoRequest;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageRequest;
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
public final class File {
  private final FileRequestRepository fileStorageRepository;
  private final FileRequestRepository fileInfoRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;
  private final Device device;
  private final String path;
  private final String name;

  public void store(byte[] content, CompletableFuture<ActionResult> futureResponse) {
    generateAvailableRequestId().thenAccept(id -> store(content, futureResponse, id));
  }

  private void store(
    byte[] content, CompletableFuture<ActionResult> futureResponse,
    UUID storeId
  ) {
    fileStorageRepository.registerFileRequest(FileRequest.create(storeId,
      device, path, name, futureResponse));
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingFileStorageRequest(storeId, device.id(),
        path, name, content));
  }

  public void info(CompletableFuture<ActionResult> futureResponse) {
    generateAvailableRequestId().thenAccept(id -> info(futureResponse, id));
  }

  private void info(
    CompletableFuture<ActionResult> futureResponse, UUID infoId
  ) {
    fileInfoRepository.registerFileRequest(FileRequest.create(infoId,
      device, path, name, futureResponse));
    if (device.platform().isMobile()) {
      findMobileFileInfo(infoId);
    } else {
      findDesktopFileInfo(infoId);
    }
  }

  private void findMobileFileInfo(UUID infoId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(identifier -> findMobileFileInfo(infoId, identifier));
  }

  private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

  private void findMobileFileInfo(UUID infoId, String identifier) {
    var requestBody = new JSONObject(Map.of("to", identifier, "data",
      Map.of("infoId", infoId, "filePath", path, "fileName", name)));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "key=" + deviceConfiguration.firebaseToken())
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }

  private void findDesktopFileInfo(UUID infoId) {
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingFileInfoRequest(infoId, device.id(),
        path, name));
  }

  public CompletableFuture<UUID> generateAvailableRequestId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    fileStorageDatabaseTable.entryExists(id).thenApply(storageExists ->
      storageExists ? generateAvailableRequestId().thenAccept(futureResponse::complete) :
        fileInfoDatabaseTable.entryExists(id).thenApply(infoExists ->
          infoExists ? generateAvailableRequestId().thenAccept(futureResponse::complete) :
            CompletableFuture.completedFuture(futureResponse.complete(id))));
    return futureResponse;
  }
}
