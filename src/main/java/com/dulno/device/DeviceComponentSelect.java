package com.dulno.device;

import com.dulno.device.structure.UserDeviceDatabaseTable;
import lombok.RequiredArgsConstructor;
import com.dulno.core.user.User;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentSelectEntry;

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
