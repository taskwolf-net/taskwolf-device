package net.taskwolf.device;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class DeviceComponentSelect implements InputComponentSelect {
  @Override
  public CompletableFuture<List<String>> compile(
    UUID id, Map<String, String> previousInputs
  ) {
    return null;
  }
}
