package com.dulno.device.file.storage;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRepository {
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private final class FileStorageRequestContent {
    private final UUID storageId;
    private final byte[] content;
  }

  private final Map<FileStorageRequestContent, ScheduledFuture<?>> storage =
    Maps.newHashMap();
  private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();

  public void registerFileContent(UUID storageId, byte[] content) {
    var schedule = executorService.schedule(() -> unregisterFileContent(storageId),
      10, TimeUnit.SECONDS);
    storage.put(new FileStorageRequestContent(storageId, content), schedule);
  }

  public void unregisterFileContent(UUID storageId) {
    var contentOptional = storage.keySet().stream()
      .filter(request -> request.storageId().equals(storageId))
      .findFirst();
    if (contentOptional.isEmpty()) {
      return;
    }
    var content = contentOptional.get();
    storage.get(content).cancel(true);
    storage.remove(content);
  }

  public Optional<byte[]> findFileContent(UUID storageId) {
    return storage.keySet().stream()
      .filter(request -> request.storageId().equals(storageId))
      .map(FileStorageRequestContent::content)
      .findFirst();
  }
}
