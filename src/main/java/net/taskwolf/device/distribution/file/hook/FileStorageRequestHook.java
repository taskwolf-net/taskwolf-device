package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.distribution.file.event.WorkerFileStorageRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;

  @EventHook
  private void fileStorageRequest(WorkerFileStorageRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    connection.get().storeFile(event.storeId(), event.filePath(), event.fileName());
  }
}

