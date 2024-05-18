package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.file.event.WorkerFileStorageResponseEvent;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.FileRequestRepository;
import net.taskwolf.device.structure.Device;

import java.util.Map;

@Singleton
public final class FileStorageResponseHook implements Hook {
  private final FileRequestRepository fileStorageRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;

  @Inject
  private FileStorageResponseHook(
    @Named("fileStorageRequestRepository") FileRequestRepository fileStorageRepository,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable
  ) {
    this.fileStorageRepository = fileStorageRepository;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
  }

  @EventHook
  private void fileStorageResponse(WorkerFileStorageResponseEvent event) {
    var optionalRequest = fileStorageRepository
      .findFileRequest(event.storageId());
    if (optionalRequest.isEmpty()) {
      return;
    }
    var request = optionalRequest.get();
    if (!event.success()) {
      request.futureResult().complete(ActionResult.failure(
        "device.action.file.store.failure.device.offline"));
      return;
    }
    long time = System.currentTimeMillis();
    request.futureResult().complete(ActionResult.success(buildInformation(
      request.device(), request.path(), request.name())));
    fileStorageRepository.unregisterFileRequest(request);
    fileStorageDatabaseTable.insertEntry(request.id(),
      request.device().id(), request.path(), request.name(), time);
  }


  private Map<String, Object> buildInformation(
    Device device, String filePath, String fileName
  ) {
    var information = device.composition();
    information.put("filePath", filePath);
    information.put("fileName", fileName);
    return information;
  }
}
