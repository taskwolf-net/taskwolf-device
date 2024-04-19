package net.taskwolf.device.distribution.file.packet.incoming;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.packet.PacketBuffer;
import net.taskwolf.core.distribution.server.packet.PacketIncoming;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingFileDeleteRequest extends PacketIncoming {
  private UUID deleteId;
  private String deviceId;
  private String filePath;
  private String fileName;

  public PacketIncomingFileDeleteRequest() {
    super(0x30);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    deleteId = buffer.readUUID();
    deviceId = buffer.readString();
    filePath = buffer.readString();
    fileName = buffer.readString();
  }
}
