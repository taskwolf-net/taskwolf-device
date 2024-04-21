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
    if (type.equals("device-file-create-trigger")) {
      return DeviceFileCreateTrigger.of(json);
    }
    if (type.equals("device-file-delete-trigger")) {
      return DeviceFileDeleteTrigger.of(json);
    }
    if (type.equals("device-folder-create-trigger")) {
      return DeviceFolderCreateTrigger.of(json);
    }
    if (type.equals("device-folder-delete-trigger")) {
      return DeviceFolderDeleteTrigger.of(json);
    }
    return null;
  }
}
