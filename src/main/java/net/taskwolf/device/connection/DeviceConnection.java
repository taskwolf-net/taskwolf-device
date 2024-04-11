package net.taskwolf.device.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.device.structure.Device;
import org.java_websocket.WebSocket;

import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DeviceConnection {
  @Getter
  private final Device device;
  @Getter
  private final WebSocket socket;

  private static final String NOTIFICATION_FORMAT = "Notification %s %s";

  public void sendNotification(String title, String body) {
    socket.send(String.format(NOTIFICATION_FORMAT, title, body));
  }

  private static final String COMMAND_FORMAT = "Command %s %s";

  public void executeCommand(UUID commandId, String command) {
    socket.send(String.format(COMMAND_FORMAT, commandId.toString(), command));
  }

  public void close() {
    socket.close();
  }
}
