package net.taskwolf.device.file.storage;

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
public final class FileStorageRedirectRepository {
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public final class FileStorageRedirectContent {
    private final String deviceId;
    private final UUID storageId;
    private final String apiKey;
    private final CompletableFuture<Map<String, Object>> futureResponse;
  }

  private final Map<FileStorageRedirectContent, ScheduledFuture<?>> redirects =
    Maps.newHashMap();
  private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();

  public void registerStorageRedirect(
    String deviceId, UUID storageId, String apiKey,
    CompletableFuture<Map<String, Object>> futureResponse
  ) {
    var schedule = executorService.schedule(() ->
      unregisterStorageRedirect(storageId), 10, TimeUnit.SECONDS);
    redirects.put(new FileStorageRedirectContent(deviceId, storageId, apiKey,
      futureResponse), schedule);
  }

  public void unregisterStorageRedirect(UUID storageId) {
    var contentOptional = redirects.keySet().stream()
      .filter(request -> request.storageId().equals(storageId))
      .findFirst();
    if (contentOptional.isEmpty()) {
      return;
    }
    var content = contentOptional.get();
    redirects.get(content).cancel(true);
    redirects.remove(content);
  }

  public Optional<FileStorageRedirectContent> findRedirectContent(
    UUID storageId
  ) {
    return redirects.keySet().stream()
      .filter(request -> request.storageId().equals(storageId))
      .findFirst();
  }
}
