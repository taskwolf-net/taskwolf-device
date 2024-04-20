package net.taskwolf.device.trigger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.trigger.Trigger;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceWorkspaceTrigger implements Trigger {
  private final String deviceId;
  private final UUID workspaceId;
}
