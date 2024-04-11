package net.taskwolf.device.connection;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.NodeType;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.distribution.packet.outgoing.PacketOutgoingCommandResponse;
import net.taskwolf.device.distribution.packet.outgoing.PacketOutgoingDeviceLogin;
import net.taskwolf.device.distribution.packet.outgoing.PacketOutgoingDeviceLogout;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
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
    DistributionClientRegistry clientRegistry, Key secretKey
  ) {
    return new DeviceWebSocket(new InetSocketAddress(port), deviceDatabaseTable,
      connectionRepository, clientRegistry, secretKey);
  }

  private final DeviceDatabaseTable deviceDatabaseTable;
  private final DeviceConnectionRepository connectionRepository;
  private final DistributionClientRegistry clientRegistry;
  private final Key secretKey;

  private DeviceWebSocket(
    InetSocketAddress address, DeviceDatabaseTable deviceDatabaseTable,
    DeviceConnectionRepository connectionRepository,
    DistributionClientRegistry clientRegistry, Key secretKey
  ) {
    super(address);
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.connectionRepository = connectionRepository;
    this.clientRegistry = clientRegistry;
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
      connection.close();
      return;
    }
    var token = matcher.group(1);
    var userId = findUserId(token);
    if (userId.isEmpty()) {
      connection.close();
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
      connection.close();
      return;
    }
    deviceDatabaseTable.findDevice(deviceId).thenAccept(device ->
      classifyConnection(userId, device, connection));
  }

  private void classifyConnection(
    UUID userId, Device device, WebSocket connection
  ) {
    if (!device.ownerId().equals(userId)) {
      connection.close();
      return;
    }
    connectionRepository.registerConnection(DeviceConnection.create(device,
      connection));
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingDeviceLogin(device.id()));
  }

  private Optional<UUID> findUserId(String token) {
    try {
      return Optional.of(UUID.fromString(Jwts.parser().setSigningKey(secretKey)
        .build().parseClaimsJws(token).getPayload().get("id", String.class)));
    } catch (Exception var3) {
      return Optional.empty();
    }
  }

  private static final String COMMAND_RESPONSE_FORMAT =
    "Command Response (.*) (.*) (.*) (.*)";

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
    clientRegistry.findClientsByType(NodeType.PROXY).stream().findFirst().get()
      .sendPacket(new PacketOutgoingCommandResponse(UUID.fromString(matcher.group(1)), true,
        matcher.group(2), matcher.group(3), Integer.valueOf(matcher.group(4))));
  }

  @Override
  public void onClose(
    WebSocket connection, int code, String reason, boolean remote
  ) {
    var proxy = findProxyClient();
    var deviceConnection = connectionRepository.findConnectionBySocket(connection);
    if (deviceConnection.isEmpty()) {
      return;
    }
    proxy.sendPacket(new PacketOutgoingDeviceLogout(deviceConnection.get()
      .device().id()));
  }

  private DistributionClient findProxyClient() {
    return clientRegistry.findClientsByType(NodeType.PROXY)
      .stream().findFirst().get();
  }

  @Override
  public void onError(WebSocket connection, Exception exception) {

  }
}