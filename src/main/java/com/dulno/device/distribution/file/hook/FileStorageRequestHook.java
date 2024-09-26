package com.dulno.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.device.connection.DeviceConnectionRepository;
import com.dulno.device.distribution.file.event.WorkerFileStorageRequestEvent;

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

