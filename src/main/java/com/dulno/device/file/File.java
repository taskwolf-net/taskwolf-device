package com.dulno.device.file;

import com.dulno.device.firebase.FirebaseRequest;
import com.dulno.device.structure.Device;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.DeviceConfiguration;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileDeleteRequest;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileInfoRequest;
import com.dulno.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageRequest;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class File {
  private final FileRequestRepository fileStorageRepository;
  private final FileRequestRepository fileInfoRepository;
  private final FileRequestRepository fileDeleteRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final WorkerProxyClient workerProxyClient;
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
    workerProxyClient.sendPacket(new PacketOutgoingFileStorageRequest(storeId,
      device.id(), device.platform(), path, name, content));
    if (device.platform().isMobile()) {
      storeMobileFile(storeId);
    }
  }

  private void storeMobileFile(UUID storeId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAcceptAsync(identifier -> storeMobileFile(storeId, identifier));
  }

  private void storeMobileFile(UUID storeId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, googleCredentials, identifier)
      .send("data", Map.of("storeId", storeId, "filePath",
        FilePath.of(path, name).compound()));
  }

  public void info(CompletableFuture<ActionResult> futureResponse) {
    generateAvailableRequestId().thenAccept(id -> info(futureResponse, id));
  }

  private void info(
    CompletableFuture<ActionResult> futureResponse, UUID infoId
  ) {
    fileInfoRepository.registerFileRequest(FileRequest.create(infoId,
      device, path, name, futureResponse));
    workerProxyClient.sendPacket(new PacketOutgoingFileInfoRequest(infoId,
      device.id(), device.platform(), path, name));
    if (device.platform().isMobile()) {
      findMobileFileInfo(infoId);
    }
  }

  private void findMobileFileInfo(UUID infoId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAcceptAsync(identifier -> findMobileFileInfo(infoId, identifier));
  }

  private void findMobileFileInfo(UUID infoId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, googleCredentials, identifier)
      .send("data", Map.of("infoId", infoId, "filePath",
        FilePath.of(path, name).compound()));
  }

  public void delete(CompletableFuture<ActionResult> futureResponse) {
    generateAvailableRequestId().thenAccept(id -> delete(futureResponse, id));
  }

  private void delete(
    CompletableFuture<ActionResult> futureResponse, UUID deleteId
  ) {
    fileDeleteRepository.registerFileRequest(FileRequest.create(deleteId,
      device, path, name, futureResponse));
    workerProxyClient.sendPacket(new PacketOutgoingFileDeleteRequest(deleteId,
      device.id(), device.platform(), path, name));
    if (device.platform().isMobile()) {
      deleteMobileFile(deleteId);
    }
  }

  private void deleteMobileFile(UUID deleteId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAcceptAsync(identifier -> deleteMobileFile(deleteId, identifier));
  }

  private void deleteMobileFile(UUID deleteId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, googleCredentials, identifier)
      .send("data", Map.of("deleteId", deleteId, "filePath",
        FilePath.of(path, name).compound()));
  }

  public CompletableFuture<UUID> generateAvailableRequestId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    fileStorageDatabaseTable.entryExists(id).thenApply(storageExists ->
      storageExists ? generateAvailableRequestId().thenAccept(futureResponse::complete) :
        fileInfoDatabaseTable.entryExists(id).thenApply(infoExists ->
          infoExists ? generateAvailableRequestId().thenAccept(futureResponse::complete) :
            fileDeleteDatabaseTable.entryExists(id).thenApply(deleteExists ->
              deleteExists ? generateAvailableRequestId().thenAccept(futureResponse::complete) :
                CompletableFuture.completedFuture(futureResponse.complete(id)))));
    return futureResponse;
  }
}
