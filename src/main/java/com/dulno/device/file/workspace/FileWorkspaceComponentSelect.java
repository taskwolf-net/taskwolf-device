package com.dulno.device.file.workspace;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import com.dulno.core.user.User;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentSelectEntry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class FileWorkspaceComponentSelect implements InputComponentSelect {
  private final FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    if (!previousInputs.containsKey("device")) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return fileWorkspaceDatabaseTable.findWorkspacesOfDevice(previousInputs.get("device"))
      .thenApply(workspaces -> workspaces.stream()
        .map(workspace -> InputComponentSelectEntry.create(workspace.id().toString(),
          workspace.path()))
        .collect(Collectors.toList()));
  }
}
