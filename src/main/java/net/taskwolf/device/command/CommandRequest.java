package net.taskwolf.device.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.action.ActionResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CommandRequest {
  private final UUID id;
  private final String device;
  private final String command;
  private final CompletableFuture<ActionResult> futureResult;
}
