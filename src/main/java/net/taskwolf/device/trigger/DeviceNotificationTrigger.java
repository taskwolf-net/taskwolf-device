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
public final class DeviceNotificationTrigger implements Trigger {
  public static TriggerInformation information(
    InputComponentSelect deviceComponentSelect
  ) {
    return TriggerInformation.builder()
      .withName("device.trigger.notification.name")
      .withDescription("device.trigger.notification.description")
      .withIdentifier("device-notification-trigger")
      .withInputVariable(InputComponentVariable.createSelect("device.trigger.notification.input.device.name",
        "device", "device.trigger.notification.input.device.description", deviceComponentSelect))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.id", "deviceId"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.name", "deviceName"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.device.platform", "devicePlatform"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.notification.title", "notificationTitle"))
      .withOutputVariable(OutputComponentVariable.create("device.trigger.notification.output.notification.body", "notificationBody"))
      .build();
  }

  public static DeviceNotificationTrigger of(JSONObject content) {
    return create(content.getString("device"));
  }

  private final String deviceId;
}
