package net.taskwolf.device.trigger;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import org.json.JSONObject;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class DeviceFolderDeleteTrigger extends DeviceWorkspaceTrigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.folder.delete.name")
      .withDescription("device.trigger.folder.delete.description")
      .withIdentifier("device-folder-delete-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.folder.delete.input.device.name",
        "device", "device.trigger.folder.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.create.input.workspace.name",
        "workspace", "device.trigger.folder.delete.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.delete.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.delete.output.folder.path", "folderPath"))
      .build();
  }

  public static DeviceFolderDeleteTrigger of(JSONObject content) {
    return create(content.getString("device"),
      UUID.fromString(content.getString("workspace")));
  }

  public static DeviceFolderDeleteTrigger create(
    String deviceId, UUID workspaceId
  ) {
    return new DeviceFolderDeleteTrigger(deviceId, workspaceId);
  }

  private DeviceFolderDeleteTrigger(String deviceId, UUID workspaceId) {
    super(deviceId, workspaceId);
  }
}
