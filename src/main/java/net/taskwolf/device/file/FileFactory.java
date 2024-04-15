package net.taskwolf.device.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import net.taskwolf.device.DeviceConfiguration;
import net.taskwolf.device.firebase.FirebaseDeviceDatabaseTable;
import net.taskwolf.device.structure.Device;

@Singleton
public final class FileFactory {
  private final FileRequestRepository fileStorageRepository;
  private final FileRequestRepository fileInfoRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final DistributionClientRegistry clientRegistry;

  @Inject
  private FileFactory(
    @Named("fileStorageRequestRepository") FileRequestRepository fileStorageRepository,
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRepository,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    DeviceConfiguration deviceConfiguration,
    DistributionClientRegistry clientRegistry
  ) {
    this.fileStorageRepository = fileStorageRepository;
    this.fileInfoRepository = fileInfoRepository;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.deviceConfiguration = deviceConfiguration;
    this.clientRegistry = clientRegistry;
  }

  public File createFile(Device device, String path, String name) {
    return File.create(fileStorageRepository, fileInfoRepository,
      fileStorageDatabaseTable, fileInfoDatabaseTable, firebaseDeviceDatabaseTable,
      deviceConfiguration, clientRegistry, device, path, name);
  }
}
