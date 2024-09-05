package net.taskwolf.device;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.user.User;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentSelectEntry;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class DeviceComponentSelect implements InputComponentSelect {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return userDeviceDatabaseTable.findAllUserDevices(target)
      .thenApply(devices -> devices.stream()
        .map(device -> InputComponentSelectEntry.create(device.deviceId(),
          device.information()))
        .collect(Collectors.toList()));
  }
}
