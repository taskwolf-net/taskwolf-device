package net.taskwolf.device.distribution.file.hook;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.distribution.file.event.FileInfoResponseEvent;
import net.taskwolf.device.file.FileDatabaseTable;
import net.taskwolf.device.file.FileHistoryDatabaseTable;
import net.taskwolf.device.file.FileRequestRepository;
import net.taskwolf.device.structure.Device;

import java.util.Map;

@Singleton
public final class FileInfoResponseHook implements Hook {
  private final FileRequestRepository fileInfoRepository;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileDatabaseTable fileDatabaseTable;

  @Inject
  private FileInfoResponseHook(
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRepository,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable,
    FileDatabaseTable fileDatabaseTable
  ) {
    this.fileInfoRepository = fileInfoRepository;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileDatabaseTable = fileDatabaseTable;
  }

  @EventHook
  private void fileInfoResponse(FileInfoResponseEvent event) {
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
    fileDatabaseTable.findEntry(event.infoId()).thenAccept(content ->
      request.futureResult().complete(ActionResult.success(buildInformation(
        request.device(), request.path(), request.name(), content))));
    fileInfoRepository.unregisterFileRequest(request);
    fileInfoDatabaseTable.insertEntry(request.id(),
      request.device().id(), request.path(), request.name(), time);
  }


  private Map<String, Object> buildInformation(
    Device device, String filePath, String fileName, byte[] fileContent
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("deviceId", device.id());
    information.put("deviceName", device.information());
    information.put("devicePlatform", device.platform());
    information.put("filePath", filePath);
    information.put("fileName", fileName);
    information.put("fileContent", fileContent);
    return information;
  }
}
