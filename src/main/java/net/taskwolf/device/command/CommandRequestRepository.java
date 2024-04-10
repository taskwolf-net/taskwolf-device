package net.taskwolf.device.command;

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
public final class CommandRequestRepository {
  private final List<CommandRequest> requests = Lists.newArrayList();

  public void registerCommandRequest(CommandRequest request) {
    requests.add(request);
  }

  public void unregisterCommandRequest(CommandRequest request) {
    requests.remove(request);
  }

  public Optional<CommandRequest> findCommandRequest(UUID commandId) {
    return requests.stream()
      .filter(request -> request.id().equals(commandId))
      .findFirst();
  }
}
