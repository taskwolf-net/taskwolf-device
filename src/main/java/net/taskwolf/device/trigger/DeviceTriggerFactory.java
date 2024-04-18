package net.taskwolf.device.trigger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerFactory;
import org.json.JSONObject;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public class DeviceTriggerFactory implements TriggerFactory {
  @Override
  public Trigger create(String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("device-notification-trigger")) {
      return DeviceNotificationTrigger.of(json);
    }
    if (type.equals("device-command-trigger")) {
      return DeviceCommandTrigger.of(json);
    }
    return null;
  }
}
