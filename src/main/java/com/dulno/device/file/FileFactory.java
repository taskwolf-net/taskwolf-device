package com.dulno.device.file;

import com.dulno.device.structure.Device;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.device.DeviceConfiguration;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;

@Singleton
public final class FileFactory {
  private final FileRequestRepository fileStorageRepository;
  private final FileRequestRepository fileInfoRepository;
  private final FileRequestRepository fileDeleteRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final DeviceConfiguration deviceConfiguration;
  private final WorkerProxyClient workerProxyClient;

  @Inject
  private FileFactory(
    @Named("fileStorageRequestRepository") FileRequestRepository fileStorageRepository,
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRepository,
    @Named("fileDeleteRequestRepository") FileRequestRepository fileDeleteRepository,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable,
    @Named("fileDeleteDatabaseTable") FileHistoryDatabaseTable fileDeleteDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    DeviceConfiguration deviceConfiguration, WorkerProxyClient workerProxyClient
  ) {
    this.fileStorageRepository = fileStorageRepository;
    this.fileInfoRepository = fileInfoRepository;
    this.fileDeleteRepository = fileDeleteRepository;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.deviceConfiguration = deviceConfiguration;
    this.workerProxyClient = workerProxyClient;
  }

  public File createFile(Device device, String path, String name) {
    return File.create(fileStorageRepository, fileInfoRepository,
      fileDeleteRepository, fileStorageDatabaseTable, fileInfoDatabaseTable,
      fileDeleteDatabaseTable, firebaseDeviceDatabaseTable, deviceConfiguration,
      workerProxyClient, device, path, name);
  }
}
