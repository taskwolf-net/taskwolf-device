package net.taskwolf.device.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileFactory {
  private final FileRequestRepository fileStorageRepository;
  private final FileRequestRepository fileInfoRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileDatabaseTable fileDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;

  public File createFile(Device device, String path) {
    return File.create(fileStorageRepository, fileInfoRepository,
      fileStorageDatabaseTable, fileInfoDatabaseTable, fileDatabaseTable,
      firebaseDeviceDatabaseTable, deviceConfiguration, clientRegistry,
      device, path);
  }
}
