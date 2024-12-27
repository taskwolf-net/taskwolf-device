package com.dulno.device.distribution.file.hook;

import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.workflow.action.ActionResult;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.distribution.file.event.WorkerFileDeleteResponseEvent;
import com.dulno.device.file.FileHistoryDatabaseTable;
import com.dulno.device.file.FileRequestRepository;

import java.util.Map;

@Singleton
public final class FileDeleteResponseHook implements Hook {
  private final FileRequestRepository fileDeleteRepository;
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;

  @Inject
  private FileDeleteResponseHook(
    @Named("fileDeleteRequestRepository") FileRequestRepository fileDeleteRepository,
    @Named("fileDeleteDatabaseTable") FileHistoryDatabaseTable fileDeleteDatabaseTable
  ) {
    this.fileDeleteRepository = fileDeleteRepository;
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
  }

  @EventHook
  private void fileDeleteResponse(WorkerFileDeleteResponseEvent event) {
    var optionalRequest = fileDeleteRepository
      .findFileRequest(event.deleteId());
    if (optionalRequest.isEmpty()) {
      return;
    }
    var request = optionalRequest.get();
    if (!event.success()) {
      request.futureResult().complete(ActionResult.failure(
        "device.action.file.delete.failure.device.offline"));
      return;
    }
    long time = System.currentTimeMillis();
    request.futureResult().complete(ActionResult.success(buildInformation(
      request.device(), request.path(), request.name())));
    fileDeleteRepository.unregisterFileRequest(request);
    fileDeleteDatabaseTable.insertEntry(request.id(),
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
