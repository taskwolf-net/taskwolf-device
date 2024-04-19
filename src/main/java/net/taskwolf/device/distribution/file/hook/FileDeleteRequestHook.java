package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.device.connection.DeviceConnectionRepository;
import net.taskwolf.device.distribution.file.event.FileDeleteRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileDeleteRequestHook implements Hook {
  private final DeviceConnectionRepository connectionRepository;

  @EventHook
  private void fileDeleteRequest(FileDeleteRequestEvent event) {
    var connection = connectionRepository.findConnection(event.deviceId());
    if (connection.isEmpty()) {
      return;
    }
    connection.get().deleteFile(event.deleteId(), event.filePath(),
      event.fileName());
  }
}

