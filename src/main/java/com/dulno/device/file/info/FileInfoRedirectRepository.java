package com.dulno.device.file.info;

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
import java.util.concurrent.*;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileInfoRedirectRepository {
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public final class FileInfoRedirectContent {
    private final String deviceId;
    private final UUID infoId;
    private final String apiKey;
    private final String content;
  }

  private final Map<FileInfoRedirectContent, ScheduledFuture<?>> redirects =
    Maps.newHashMap();
  private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();

  public void registerInfoRedirect(
    String deviceId, UUID infoId, String apiKey, String content
  ) {
    var schedule = executorService.schedule(() ->
      unregisterInfoRedirect(infoId), 10, TimeUnit.SECONDS);
    redirects.put(new FileInfoRedirectContent(deviceId, infoId, apiKey, content),
      schedule);
  }

  public void unregisterInfoRedirect(UUID infoId) {
    var contentOptional = redirects.keySet().stream()
      .filter(request -> request.infoId().equals(infoId))
      .findFirst();
    if (contentOptional.isEmpty()) {
      return;
    }
    var content = contentOptional.get();
    redirects.get(content).cancel(true);
    redirects.remove(content);
  }

  public Optional<FileInfoRedirectContent> findRedirectContent(UUID infoId) {
    return redirects.keySet().stream()
      .filter(request -> request.infoId().equals(infoId))
      .findFirst();
  }
}
