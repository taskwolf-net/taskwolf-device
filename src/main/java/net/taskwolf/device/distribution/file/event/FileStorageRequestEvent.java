package net.taskwolf.device.distribution.file.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.event.Event;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileStorageRequestEvent extends Event {
  private final UUID storeId;
  private final String deviceId;
  private final String filePath;
  private final String fileName;
  private final DistributionClient client;
}
