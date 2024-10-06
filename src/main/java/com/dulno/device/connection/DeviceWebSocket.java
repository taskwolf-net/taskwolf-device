package com.dulno.device.connection;

import com.dulno.device.structure.Device;
import com.dulno.device.structure.DeviceDatabaseTable;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.distribution.command.packet.outgoing.PacketOutgoingCommandResponse;
import com.dulno.device.distribution.device.packet.outgoing.PacketOutgoingDeviceLogin;
import com.dulno.device.distribution.device.packet.outgoing.PacketOutgoingDeviceLogout;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.security.Key;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(staticName = "create")
public final class DeviceWebSocket extends WebSocketServer {
  public static DeviceWebSocket of(
    int port, DeviceDatabaseTable deviceDatabaseTable,
    DeviceConnectionRepository connectionRepository,
    WorkerProxyClient workerProxyClient, Key secretKey
  ) {
    var socket = new DeviceWebSocket(new InetSocketAddress(port),
      deviceDatabaseTable, connectionRepository, workerProxyClient, secretKey);
    socket.setReuseAddr(true);
    return socket;
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final DeviceConnectionRepository connectionRepository;
  private final WorkerProxyClient workerProxyClient;
  private final Key secretKey;

  private DeviceWebSocket(
    InetSocketAddress address, DeviceDatabaseTable deviceDatabaseTable,
    DeviceConnectionRepository connectionRepository,
    WorkerProxyClient workerProxyClient, Key secretKey
  ) {
    super(address);
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.connectionRepository = connectionRepository;
    this.workerProxyClient = workerProxyClient;
    this.secretKey = secretKey;
  }

  @Override
  public void onStart() {
    setConnectionLostTimeout(1);
  }

  private static final String HANDSHAKE_FORMAT = "/device/connect/?token=(.*)&device=(.*)";

  @Override
  public void onOpen(WebSocket connection, ClientHandshake handshake) {
    var pattern = Pattern.compile(HANDSHAKE_FORMAT.replace("?", ""));
    var matcher = pattern.matcher(handshake.getResourceDescriptor().replace("?", ""));
    if (!matcher.matches()) {
      rejectConnection(connection);
      return;
    }
    var token = matcher.group(1);
    var userId = findUserId(token);
    if (userId.isEmpty()) {
      rejectConnection(connection);
      return;
    }
    var device = matcher.group(2);
    deviceDatabaseTable.deviceExists(device).thenAccept(exists ->
      classifyConnection(userId.get(), device, connection, exists));
  }

  private void classifyConnection(
    UUID userId, String deviceId, WebSocket connection, boolean exists
  ) {
    if (!exists) {
      rejectConnection(connection);
      return;
    }
    deviceDatabaseTable.findDevice(deviceId).thenAccept(device ->
      classifyConnection(userId, device, connection));
  }

  private void classifyConnection(
    UUID userId, Device device, WebSocket connection
  ) {
    if (!device.ownerId().equals(userId)) {
      rejectConnection(connection);
      return;
    }
    connectionRepository.registerConnection(DeviceConnection.create(device,
      connection));
    workerProxyClient.sendPacket(new PacketOutgoingDeviceLogin(device.id()));
  }

  private void rejectConnection(WebSocket connection) {
    connection.send("REJECTED");
    connection.close();
  }

  private Optional<UUID> findUserId(String token) {
    try {
      return Optional.of(UUID.fromString(Jwts.parser().setSigningKey(secretKey)
        .build().parseClaimsJws(token).getPayload().get("id", String.class)));
    } catch (Exception exception) {
      return Optional.empty();
    }
  }

  private static final String COMMAND_RESPONSE_FORMAT =
    "Command Response (.*) '(.*)' '(.*)' (.*)";

  @Override
  public void onMessage(WebSocket connection, String message) {
    var deviceConnection = connectionRepository.findConnectionBySocket(connection);
    if (deviceConnection.isEmpty()) {
      return;
    }
    var pattern = Pattern.compile(COMMAND_RESPONSE_FORMAT);
    var matcher = pattern.matcher(message);
    if (!matcher.matches()) {
      return;
    }
    try {
      processCommandResponse(matcher);
    } catch (Exception ignored) {
    }
  }

  private void processCommandResponse(Matcher matcher) throws Exception {
    workerProxyClient.sendPacket(new PacketOutgoingCommandResponse(
      UUID.fromString(matcher.group(1)), true, matcher.group(2), matcher.group(3),
      Integer.valueOf(matcher.group(4))));
  }

  @Override
  public void onClose(
    WebSocket connection, int code, String reason, boolean remote
  ) {
    var deviceConnectionOptional = connectionRepository
      .findConnectionBySocket(connection);
    if (deviceConnectionOptional.isEmpty()) {
      return;
    }
    var deviceConnection = deviceConnectionOptional.get();
    connectionRepository.unregisterConnection(deviceConnection);
    workerProxyClient.sendPacket(new PacketOutgoingDeviceLogout(
      deviceConnection.device().id()));
  }

  @Override
  public void onError(WebSocket connection, Exception exception) {

  }
}