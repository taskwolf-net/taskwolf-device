package net.taskwolf.device.distribution.file.hook;

import net.taskwolf.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.file.event.WorkerFileDeleteResponseEvent;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.FileRequestRepository;

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
