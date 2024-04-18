package net.taskwolf.device.file.storage;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRepository {
  private final Map<UUID, byte[]> storage = Maps.newHashMap();

  public void registerFileContent(UUID storageId, byte[] content) {
    storage.put(storageId, content);
  }

  public void unregisterFileContent(UUID storageId) {
    storage.remove(storageId);
  }

  public Optional<byte[]> findFileContent(UUID storageId) {
    return Optional.ofNullable(storage.get(storageId));
  }
}
