package net.taskwolf.device;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.user.User;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class DeviceComponentSelect implements InputComponentSelect {
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;

  @Override
  public CompletableFuture<List<String>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    var futureResponse = new CompletableFuture<List<String>>();
    userDeviceDatabaseTable.findAllUserDevices(target).thenAccept(devices ->
      devices.stream().map(device -> new JSONObject(Map.of("identifier",
          device.deviceId(), "name", device.information())).toString())
        .collect(Collectors.toList()));
    return futureResponse;
  }
}
