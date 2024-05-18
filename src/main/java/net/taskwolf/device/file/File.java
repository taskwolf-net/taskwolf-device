package net.taskwolf.device.file;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileDeleteRequest;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileInfoRequest;
import net.taskwolf.device.distribution.file.packet.outgoing.PacketOutgoingFileStorageRequest;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.firebase.FirebaseRequest;
import net.taskwolf.device.structure.Device;

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
      device.id(), path, name, content));
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

  private void findMobileFileInfo(UUID infoId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, identifier).send("data",
      Map.of("infoId", infoId, "filePath", FilePath.of(path, name).compound()));
  }

  private void findDesktopFileInfo(UUID infoId) {
    workerProxyClient.sendPacket(new PacketOutgoingFileInfoRequest(infoId,
      device.id(), path, name));
  }

  public void delete(CompletableFuture<ActionResult> futureResponse) {
    generateAvailableRequestId().thenAccept(id -> delete(futureResponse, id));
  }

  private void delete(
    CompletableFuture<ActionResult> futureResponse, UUID deleteId
  ) {
    fileDeleteRepository.registerFileRequest(FileRequest.create(deleteId,
      device, path, name, futureResponse));
    if (device.platform().isMobile()) {
      deleteMobileFile(deleteId);
    } else {
      deleteDesktopFile(deleteId);
    }
  }

  private void deleteMobileFile(UUID deleteId) {
    firebaseDeviceDatabaseTable.findDeviceIdentifier(device.id())
      .thenAccept(identifier -> deleteMobileFile(deleteId, identifier));
  }

  private void deleteMobileFile(UUID deleteId, String identifier) {
    FirebaseRequest.create(deviceConfiguration, identifier).send("data",
      Map.of("deleteId", deleteId, "filePath", FilePath.of(path, name).compound()));
  }

  private void deleteDesktopFile(UUID deleteId) {
    workerProxyClient.sendPacket(new PacketOutgoingFileDeleteRequest(deleteId,
      device.id(), path, name));
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
