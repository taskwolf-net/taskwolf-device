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

  private static final String FILE_STORE_FORMAT = "File Storage %s %s %s";

  public void storeFile(UUID storeId, String path, String name) {
    socket.send(String.format(FILE_STORE_FORMAT, storeId.toString(), path, name));
  }

  private static final String FILE_INFO_FORMAT = "File Info %s %s %s";

  public void fileInfo(UUID infoId, String path, String name) {
    socket.send(String.format(FILE_INFO_FORMAT, infoId.toString(), path, name));
  }

  public void close() {
    socket.close();
  }
}
