package net.taskwolf.device.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.device.structure.Device;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FileRequest {
  private final UUID id;
  private final Device device;
  private final String path;
  private final String name;
  private final CompletableFuture<ActionResult> futureResult;
}
