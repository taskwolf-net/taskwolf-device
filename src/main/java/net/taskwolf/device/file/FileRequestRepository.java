package net.taskwolf.device.file;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(staticName = "create")
public final class FileRequestRepository {
  private final Map<FileRequest, ScheduledFuture<?>> requests = Maps.newHashMap();
  private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();

  public void registerFileRequest(FileRequest request) {
    var schedule = executorService.schedule(() -> unregisterFileRequest(request),
      10, TimeUnit.SECONDS);
    requests.put(request, schedule);
  }

  public void unregisterFileRequest(FileRequest request) {
    if (!requests.containsKey(request)) {
      return;
    }
    requests.get(request).cancel(true);
    requests.remove(request);
  }

  public Optional<FileRequest> findFileRequest(UUID requestId) {
    return requests.keySet().stream()
      .filter(request -> request.id().equals(requestId))
      .findFirst();
  }
}
