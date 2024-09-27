package com.dulno.device;

import com.dulno.device.file.FileRequestRepository;
import com.dulno.device.file.info.FileInfoRedirectRepository;
import com.dulno.device.file.storage.FileStorageRedirectRepository;
import com.dulno.device.structure.DeviceDatabaseTable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.device.command.CommandExecutionDatabaseTable;
import com.dulno.device.command.CommandRequestRepository;
import com.dulno.device.file.FileHistoryDatabaseTable;
import com.dulno.device.file.storage.FileStorageRepository;
import com.dulno.device.file.workspace.FileWorkspaceDatabaseTable;
import com.dulno.device.firebase.FirebaseDeviceDatabaseTable;
import com.dulno.device.notification.NotificationFactory;
import com.dulno.device.structure.UserDeviceDatabaseTable;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
public final class DeviceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DeviceDatabaseTable deviceDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable;
  private final NotificationFactory notificationFactory;
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final CommandRequestRepository commandRequestRepository;
  private final FileHistoryDatabaseTable fileStorageDatabaseTable;
  private final FileHistoryDatabaseTable fileInfoDatabaseTable;
  private final FileHistoryDatabaseTable fileDeleteDatabaseTable;
  private final FileStorageRepository fileStorageRepository;
  private final FileStorageRedirectRepository fileStorageRedirectRepository;
  private final FileRequestRepository fileInfoRequestRepository;
  private final FileInfoRedirectRepository fileInfoRedirectRepository;
  private final FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable;

  @Inject
  private DeviceContextInitializer(
    DeviceDatabaseTable deviceDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    FirebaseDeviceDatabaseTable firebaseDeviceDatabaseTable,
    NotificationFactory notificationFactory,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    CommandRequestRepository commandRequestRepository,
    @Named("fileStorageDatabaseTable") FileHistoryDatabaseTable fileStorageDatabaseTable,
    @Named("fileInfoDatabaseTable") FileHistoryDatabaseTable fileInfoDatabaseTable,
    @Named("fileDeleteDatabaseTable") FileHistoryDatabaseTable fileDeleteDatabaseTable,
    FileStorageRepository fileStorageRepository,
    FileStorageRedirectRepository fileStorageRedirectRepository,
    @Named("fileInfoRequestRepository") FileRequestRepository fileInfoRequestRepository,
    FileInfoRedirectRepository fileInfoRedirectRepository,
    FileWorkspaceDatabaseTable fileWorkspaceDatabaseTable
  ) {
    this.deviceDatabaseTable = deviceDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.firebaseDeviceDatabaseTable = firebaseDeviceDatabaseTable;
    this.notificationFactory = notificationFactory;
    this.commandExecutionDatabaseTable = commandExecutionDatabaseTable;
    this.commandRequestRepository = commandRequestRepository;
    this.fileStorageDatabaseTable = fileStorageDatabaseTable;
    this.fileInfoDatabaseTable = fileInfoDatabaseTable;
    this.fileDeleteDatabaseTable = fileDeleteDatabaseTable;
    this.fileStorageRepository = fileStorageRepository;
    this.fileStorageRedirectRepository = fileStorageRedirectRepository;
    this.fileInfoRequestRepository = fileInfoRequestRepository;
    this.fileInfoRedirectRepository = fileInfoRedirectRepository;
    this.fileWorkspaceDatabaseTable = fileWorkspaceDatabaseTable;
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("deviceDatabaseTable", deviceDatabaseTable);
    beanFactory.registerSingleton("userDeviceDatabaseTable", userDeviceDatabaseTable);
    beanFactory.registerSingleton("firebaseDeviceDatabaseTable", firebaseDeviceDatabaseTable);
    beanFactory.registerSingleton("notificationFactory", notificationFactory);
    beanFactory.registerSingleton("commandExecutionDatabaseTable", commandExecutionDatabaseTable);
    beanFactory.registerSingleton("commandRequestRepository", commandRequestRepository);
    beanFactory.registerSingleton("fileStorageDatabaseTable", fileStorageDatabaseTable);
    beanFactory.registerSingleton("fileInfoDatabaseTable", fileInfoDatabaseTable);
    beanFactory.registerSingleton("fileDeleteDatabaseTable", fileDeleteDatabaseTable);
    beanFactory.registerSingleton("fileStorageRepository", fileStorageRepository);
    beanFactory.registerSingleton("fileStorageRedirectRepository", fileStorageRedirectRepository);
    beanFactory.registerSingleton("fileInfoRequestRepository", fileInfoRequestRepository);
    beanFactory.registerSingleton("fileInfoRedirectRepository", fileInfoRedirectRepository);
    beanFactory.registerSingleton("fileWorkspaceDatabaseTable", fileWorkspaceDatabaseTable);
  }
}
