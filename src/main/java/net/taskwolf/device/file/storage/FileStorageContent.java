package net.taskwolf.device.file.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileStorageContent {
  private final UUID storageId;
  private final byte[] content;
}
