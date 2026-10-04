package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.workflow.action.ActionResult;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.file.event.WorkerFileInfoResponseEvent;
import net.taskwolf.device.file.FileRequestRepository;

@Singleton
public final class FileInfoResponseHook implements Hook {
  private final FileRequestRepository fileInfoRepository;

  @Inject
  private FileInfoResponseHook(
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRepository
  ) {
    this.fileInfoRepository = fileInfoRepository;
  }

  @EventHook
  private void fileInfoResponse(WorkerFileInfoResponseEvent event) {
    var optionalRequest = fileInfoRepository.findFileRequest(event.infoId());
    if (optionalRequest.isEmpty()) {
      return;
    }
    var request = optionalRequest.get();
    if (event.success()) {
      return;
    }
    request.futureResult().complete(ActionResult.failure(
      "device.action.file.info.failure.device.offline"));
  }
}
