package net.taskwolf.device.file;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileRequestRepository {
  private final List<FileRequest> requests = Lists.newArrayList();

  public void registerFileRequest(FileRequest request) {
    requests.add(request);
  }

  public void unregisterFileRequest(FileRequest request) {
    requests.remove(request);
  }

  public Optional<FileRequest> findFileRequest(UUID requestId) {
    return requests.stream()
      .filter(request -> request.id().equals(requestId))
      .findFirst();
  }
}
