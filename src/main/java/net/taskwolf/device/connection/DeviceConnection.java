package net.taskwolf.device.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.device.structure.Device;
import org.java_websocket.WebSocket;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DeviceConnection {
  @Getter
  private final Device device;
  @Getter
  private final WebSocket socket;
}
