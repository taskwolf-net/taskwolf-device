package net.taskwolf.device.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.device.file.FilePath;
import net.taskwolf.device.structure.Device;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class DeviceConnection {
  @Getter
  private final Device device;
  @Getter
  private final WebSocket socket;

  public void sendNotification(String title, String body) {
    socket.send(new JSONObject(Map.of("type", "NOTIFICATION", "title", title,
      "body", body)).toString());
  }

  public void executeCommand(UUID commandId, String command) {
    socket.send(new JSONObject(Map.of("type", "COMMAND",
      "commandId", commandId, "command", command)).toString());
  }

  public void storeFile(UUID storeId, String path, String name) {
    socket.send(new JSONObject(Map.of("type", "FILE_STORAGE",
      "storeId", storeId, "filePath", FilePath.of(path, name).compound()))
      .toString());
  }

  public void fileInfo(UUID infoId, String path, String name) {
    socket.send(new JSONObject(Map.of("type", "FILE_INFO",
      "infoId", infoId, "filePath", FilePath.of(path, name).compound()))
      .toString());
  }

  public void deleteFile(UUID deleteId, String path, String name) {
    socket.send(new JSONObject(Map.of("type", "FILE_DELETE",
      "deleteId", deleteId, "filePath", FilePath.of(path, name).compound()))
      .toString());
  }

  public void close() {
    socket.close();
  }
}
