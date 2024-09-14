package com.dulno.device.distribution.file.hook;

import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.action.ActionResult;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.distribution.file.event.WorkerFileInfoResponseEvent;
import com.dulno.device.file.FileHistoryDatabaseTable;
import com.dulno.device.file.FileRequestRepository;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Map;

@Singleton
public final class FileInfoResponseHook implements Hook {
  private final FileRequestRepository fileInfoRepository;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;

  @Inject
  private FileInfoResponseHook(
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRepository,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable
  ) {
    this.fileInfoRepository = fileInfoRepository;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
  }

  @EventHook
  private void fileInfoResponse(WorkerFileInfoResponseEvent event) {
    var optionalRequest = fileInfoRepository.findFileRequest(event.infoId());
    if (optionalRequest.isEmpty()) {
      return;
    }
    var request = optionalRequest.get();
    if (!event.success()) {
      request.futureResult().complete(ActionResult.failure(
        "device.action.file.info.failure.device.offline"));
      return;
    }
    long time = System.currentTimeMillis();
    request.futureResult().complete(ActionResult.success(buildInformation(
      request.device(), request.path(), request.name(), event.content())));
    fileInfoRepository.unregisterFileRequest(request);
    fileInfoDatabaseTable.insertEntry(request.id(),
      request.device().id(), request.path(), request.name(), time);
  }

  private Map<String, Object> buildInformation(
          Device device, String filePath, String fileName, byte[] fileContent
  ) {
    var information = device.composition();
    information.put("filePath", filePath);
    information.put("fileName", fileName);
    information.put("fileContent", Base64.encodeBase64String(fileContent));
    return information;
  }
}
