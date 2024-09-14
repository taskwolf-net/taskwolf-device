package com.dulno.device.command;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class CommandRequestRepository {
  private final Map<CommandRequest, ScheduledFuture<?>> requests = Maps.newHashMap();
  private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();

  public void registerCommandRequest(CommandRequest request) {
    var schedule = executorService.schedule(() -> unregisterCommandRequest(request),
      10, TimeUnit.SECONDS);
    requests.put(request, schedule);
  }

  public void unregisterCommandRequest(CommandRequest request) {
    if (!requests.containsKey(request)) {
      return;
    }
    requests.get(request).cancel(true);
    requests.remove(request);
  }

  public Optional<CommandRequest> findCommandRequest(UUID commandId) {
    return requests.keySet().stream()
      .filter(request -> request.id().equals(commandId))
      .findFirst();
  }
}
