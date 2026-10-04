package net.taskwolf.device.file.workspace;

import net.taskwolf.device.structure.UserDeviceDatabaseTable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.user.User;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentSelectEntry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class FileWorkspaceComponentSelect implements InputComponentSelect {
  private final FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    try {
      var deviceId = previousInputs.get("device");
      return userDeviceDatabaseTable.userHasDevice(target, deviceId)
        .thenCompose(has -> checkDeviceAccess(deviceId, has));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
  }

  private CompletableFuture<List<InputComponentSelectEntry>> checkDeviceAccess(
    String deviceId, boolean hasAccess
  ) {
    if (!hasAccess) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return fileWorkspaceDatabaseTable.findWorkspacesOfDevice(deviceId)
      .thenApply(workspaces -> workspaces.stream()
        .map(workspace -> InputComponentSelectEntry.create(
          workspace.id().toString(), workspace.path()))
        .toList());
  }
}
