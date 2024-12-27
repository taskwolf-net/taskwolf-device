package com.dulno.device.command;

import com.dulno.device.structure.Device;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.workflow.action.ActionResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class CommandRequest {
  private final UUID id;
  private final Device device;
  private final String command;
  private final CompletableFuture<ActionResult> futureResult;
}
