package net.taskwolf.device.file.workspace;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public class FileWorkspaceComponentSelect implements InputComponentSelect {
  private final FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable;

  @Override
  public CompletableFuture<List<String>> compile(
    UUID id, Map<String, String> previousInputs
  ) {
    if (!previousInputs.containsKey("device")) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    var futureResponse = new CompletableFuture<List<String>>();
    fileWorkspaceDatabaseTable.findWorkspacesOfDevice(previousInputs.get("device"))
      .thenAccept(workspaces -> futureResponse.complete(workspaces.stream()
        .map(workspace -> new JSONObject(Map.of("identifier", workspace.id(),
          "name", workspace.path())).toString()).collect(Collectors.toList())));
    return futureResponse;
  }
}
