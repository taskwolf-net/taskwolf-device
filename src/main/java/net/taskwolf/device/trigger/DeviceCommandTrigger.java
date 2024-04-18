package net.taskwolf.device.trigger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerInformation;
import net.taskwolf.core.workflow.component.input.InputComponentSelect;
import net.taskwolf.core.workflow.component.input.InputComponentVariable;
import net.taskwolf.core.workflow.component.output.OutputComponentVariable;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DeviceCommandTrigger implements Trigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.command.name")
      .withDescription("device.trigger.command.description")
      .withIdentifier("device-command-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.command.input.device.name",
        "device", "device.trigger.command.input.device.description", deviceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command", "command"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.output", "commandOutput"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.error.message", "commandErrorMessage"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.exit.code", "commandExitCode"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.command.output.command.execution.time", "commandExecutionTime"))
      .build();
  }

  public static DeviceCommandTrigger of(JSONObject content) {
    return create(content.getString("device"));
  }

  private final String deviceId;
}
