package com.dulno.device.distribution.file.hook;

import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.action.ActionResult;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.distribution.file.event.WorkerFileStorageResponseEvent;
import com.dulno.device.file.FileHistoryDatabaseTable;
import com.dulno.device.file.FileRequestRepository;

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
