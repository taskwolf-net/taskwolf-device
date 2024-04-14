package net.taskwolf.device.file;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
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
