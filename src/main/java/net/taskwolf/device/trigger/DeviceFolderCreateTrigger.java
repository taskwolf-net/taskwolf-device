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
public final class DeviceFolderCreateTrigger extends DeviceWorkspaceTrigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.folder.create.name")
      .withDescription("device.trigger.folder.create.description")
      .withIdentifier("device-folder-create-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.folder.create.input.device.name",
        "device", "device.trigger.folder.create.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.create.input.workspace.name",
        "workspace", "device.trigger.folder.create.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.folder.create.output.folder.path", "folderPath"))
      .build();
  }

  public static DeviceFolderCreateTrigger of(JSONObject content) {
    return create(content.getString("device"),
      UUID.fromString(content.getString("workspace")));
  }

  public static DeviceFolderCreateTrigger create(
    String deviceId, UUID workspaceId
  ) {
    return new DeviceFolderCreateTrigger(deviceId, workspaceId);
  }

  private DeviceFolderCreateTrigger(String deviceId, UUID workspaceId) {
    super(deviceId, workspaceId);
  }
}
