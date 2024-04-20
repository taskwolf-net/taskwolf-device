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
public final class DeviceFileDeleteTrigger extends DeviceWorkspaceTrigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.file.delete.name")
      .withDescription("device.trigger.file.delete.description")
      .withIdentifier("device-file-delete-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.delete.input.device.name",
        "device", "device.trigger.file.delete.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.delete.input.workspace.name",
        "workspace", "device.trigger.file.delete.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.delete.output.file.name", "fileName"))
      .build();
  }

  public static DeviceFileDeleteTrigger of(JSONObject content) {
    return create(content.getString("device"),
      UUID.fromString(content.getString("workspace")));
  }

  public static DeviceFileDeleteTrigger create(
    String deviceId, UUID workspaceId
  ) {
    return new DeviceFileDeleteTrigger(deviceId, workspaceId);
  }

  private DeviceFileDeleteTrigger(String deviceId, UUID workspaceId) {
    super(deviceId, workspaceId);
  }
}
