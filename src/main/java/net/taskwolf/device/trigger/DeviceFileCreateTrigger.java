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
public final class DeviceFileCreateTrigger extends DeviceWorkspaceTrigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect,
    InputComponentSelect workspaceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.file.create.name")
      .withDescription("device.trigger.file.create.description")
      .withIdentifier("device-file-create-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.create.input.device.name",
        "device", "device.trigger.file.create.input.device.description", deviceComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.file.create.input.workspace.name",
        "workspace", "device.trigger.file.create.input.workspace.description", workspaceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.workspace", "workspace"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.file.path", "filePath"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.file.create.output.file.name", "fileName"))
      .build();
  }

  public static DeviceFileCreateTrigger of(JSONObject content) {
    return create(content.getString("device"),
      UUID.fromString(content.getString("workspace")));
  }

  public static DeviceFileCreateTrigger create(
    String deviceId, UUID workspaceId
  ) {
    return new DeviceFileCreateTrigger(deviceId, workspaceId);
  }

  private DeviceFileCreateTrigger(String deviceId, UUID workspaceId) {
    super(deviceId, workspaceId);
  }
}
