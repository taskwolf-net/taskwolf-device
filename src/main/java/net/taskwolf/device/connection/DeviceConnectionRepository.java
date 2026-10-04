package net.taskwolf.device.connection;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.java_websocket.WebSocket;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DeviceConnectionRepository {
  private final List<DeviceConnection> connections = Lists.newArrayList();

  public void registerConnection(DeviceConnection connection) {
    connections.add(connection);
  }

  public void unregisterConnection(DeviceConnection connection) {
    connections.remove(connection);
  }

  public Optional<DeviceConnection> findConnection(String deviceId) {
    return connections.stream()
      .filter(connection -> connection.device().id().equals(deviceId))
      .findFirst();
  }

  public Optional<DeviceConnection> findConnectionBySocket(WebSocket socket) {
    return connections.stream()
      .filter(connection -> connection.socket().equals(socket))
      .findFirst();
  }

  public List<DeviceConnection> allConnection() {
    return List.copyOf(connections);
  }
}
