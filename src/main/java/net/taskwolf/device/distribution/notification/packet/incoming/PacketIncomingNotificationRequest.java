package net.taskwolf.device.distribution.notification.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingNotificationRequest extends PacketIncoming {
  private UUID notificationId;
  private String deviceId;
  private String title;
  private String body;

  public PacketIncomingNotificationRequest() {
    super(0x22);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    notificationId = buffer.readUUID();
    deviceId = buffer.readString();
    title = buffer.readString();
    body = buffer.readString();
  }
}
