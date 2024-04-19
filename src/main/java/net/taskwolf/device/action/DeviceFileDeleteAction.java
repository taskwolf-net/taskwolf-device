package net.taskwolf.device.action;

import lombok.AllArgsConstructor;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionInformation;
import net.taskwolf.core.action.ActionResult;
import net.taskwolf.core.workflow.component.input.InputComponentDataType;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import net.taskwolf.core.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.device.file.FileFactory;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class DeviceFileDeleteAction implements Action {
  public static ActionInformation information(
    InputComponentSelect deviceComponentSelect
  ) {
    return ActionInformation.builder()
      .withName("device.action.file.delete.name")
      .withDescription("device.action.file.delete.description")
      .withIdentifier("device-file-delete-action")
      .withInputVariable(InputComponentVariable.createSelect("device.action.file.delete.input.device.name",
        "device", "device.action.file.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.delete.input.file.path.name",
        "filePath", "device.action.file.delete.input.file.path.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("device.action.file.delete.input.file.name.name",
        "fileName", "device.action.file.delete.input.file.name.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.action.file.delete.output.file.name", "fileName"))
      .build();
  }

  public static DeviceFileDeleteAction of(
    DeviceDatabaseTable deviceDatabaseTable, FileFactory fileFactory,
    JSONObject content
  ) {
    return create(deviceDatabaseTable, fileFactory,
      content.getString("device"), content.getString("filePath"),
      content.getString("fileName"));
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final FileFactory fileFactory;
  private final String deviceId;
  private String filePath;
  private String fileName;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    filePath = dissolve.dissolve(filePath);
    fileName = dissolve.dissolve(fileName);
    return deviceDatabaseTable.deviceExists(deviceId)
      .thenCompose(this::storeFile);
  }

  private CompletableFuture<ActionResult> storeFile(boolean deviceExists) {
    if (!deviceExists) {
      return ActionResult.futureFailure("device.action.file.delete.failure.device.not.found");
    }
    return deviceDatabaseTable.findDevice(deviceId)
      .thenCompose(this::storeFile);
  }

  private CompletableFuture<ActionResult> storeFile(Device device) {
    if (!device.fileDelete()) {
      return ActionResult.futureFailure("device.action.file.delete.failure.device.permission");
    }
    var futureResponse = new CompletableFuture<ActionResult>();
    fileFactory.createFile(device, filePath, fileName).delete(futureResponse);
    return futureResponse;
  }
}
