package com.dulno.device.access;

import com.dulno.access.verification.Verification;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.environment.DulnoEnvironment;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.hashing.Hashing;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import com.dulno.device.structure.*;
import com.dulno.access.verification.VerificationLoginController;
import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class DeviceScanController extends DeviceController {
  private final Key homeKey;
  private final Key refreshKey;
  private final DeviceScanDatabaseTable deviceScanDatabaseTable;
  private final UserDeviceDatabaseTable userDeviceDatabaseTable;
  private final DeviceModificationController deviceModificationController;
  private final VerificationLoginController verificationLoginController;
  private final ErrorRepository errorRepository;
  private final DulnoEnvironment environment;
  private final Hashing hashing;

  private DeviceScanController(
    @Qualifier("homeKey") Key homeKey, @Qualifier("productKey") Key productKey,
    @Qualifier("refreshKey") Key refreshKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    DeviceScanDatabaseTable deviceScanDatabaseTable,
    UserDeviceDatabaseTable userDeviceDatabaseTable,
    DeviceModificationController deviceModificationController,
    VerificationLoginController verificationLoginController,
    ErrorRepository errorRepository, DulnoEnvironment environment, Hashing hashing
  ) {
    super(productKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.homeKey = homeKey;
    this.refreshKey = refreshKey;
    this.deviceScanDatabaseTable = deviceScanDatabaseTable;
    this.userDeviceDatabaseTable = userDeviceDatabaseTable;
    this.deviceModificationController = deviceModificationController;
    this.verificationLoginController = verificationLoginController;
    this.errorRepository = errorRepository;
    this.environment = environment;
    this.hashing = hashing;
  }

  @RequestMapping(path = "/device/scan/create/", method = RequestMethod.GET)
  public CompletableFuture<Map<String, Object>> createDeviceScan(
    HttpServletRequest request
  ) {
    return findUser(request).thenCompose(user -> findDeviceTarget(user.id())
      .thenCompose(target -> deviceScanDatabaseTable.generateAvailableScanId()
        .thenCompose(id -> createDeviceScan(user, target, id))));
  }

  private CompletableFuture<Map<String, Object>> createDeviceScan(
    User user, UUID targetId, UUID scanId
  ) {
    var token = UUID.randomUUID();
    var qrCode = generateScanQrCode(scanId, token);
    return deviceScanDatabaseTable.insertScan(scanId, user.id(), targetId, token,
        "", "", "", "", false, false)
      .thenApply(value -> Map.of("success", true, "scan", scanId,
        "qrCode", qrCode));
  }

  private static final int QR_CODE_SIZE = 256;
  private static final String QR_CODE_CONTENT = "https://%s/device/scan/%s/%s/";

  private String generateScanQrCode(UUID scanId, UUID token) {
    try {
      var payload = String.format(QR_CODE_CONTENT, environment.domain(),
        scanId, token);
      var qrCodeWriter = new QRCodeWriter();
      var bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE,
        QR_CODE_SIZE, QR_CODE_SIZE);
      var bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
      var outputStream = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", outputStream);
      var qrCodeBytes = outputStream.toByteArray();
      return Base64.getEncoder().encodeToString(qrCodeBytes);
    } catch (Exception exception) {
      errorRepository.processError(exception);
      return "";
    }
  }

  @RequestMapping(path = "/device/scan/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> scanDevice(
    @RequestBody String payload, HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var scanId = body.getUUID("scan");
    var token = body.getUUID("token");
    var machine = body.getString("device");
    var information = body.getSanitizedString("information", 64);
    var platform = DevicePlatform.valueOf(body.getString("platform").toUpperCase());
    var firebaseToken = platform.isAndroid() ? body.getString("firebaseToken") : "";
    return deviceScanDatabaseTable.scanExists(scanId)
      .thenCompose(exists -> scanDevice(scanId, token, machine, information,
        platform.toString(), firebaseToken, exists));
  }

  private CompletableFuture<Map<String, Object>> scanDevice(
    UUID scanId, UUID token, String machine, String information,
    String platform, String firebaseToken, boolean scanExists
  ) {
    if (!scanExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1000));
    }
    return deviceScanDatabaseTable.findScan(scanId)
      .thenCompose(scan -> scanDevice(scan, token, machine, information,
        platform, firebaseToken));
  }

  private CompletableFuture<Map<String, Object>> scanDevice(
    DeviceScan scan, UUID token, String machine, String information,
    String platform, String firebaseToken
  ) {
    if (!scan.token().equals(token)) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1001));
    }
    if (scan.scanned()) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1002));
    }
    return deviceScanDatabaseTable.performScan(scan, machine, information,
      platform, firebaseToken).thenApply(value -> Map.of("success", true));
  }

  @RequestMapping(path = "/device/scan/decide/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> decideDeviceScan(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var scanId = body.getUUID("scan");
    var approved = body.getBoolean("approved");
    return findUser(request)
      .thenCompose(user -> deviceScanDatabaseTable.scanExists(scanId)
        .thenCompose(exists -> decideDeviceScan(user, scanId, approved, exists)));
  }

  private CompletableFuture<Map<String, Object>> decideDeviceScan(
    User user, UUID scanId, boolean approved, boolean scanExists
  ) {
    if (!scanExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1000));
    }
    return deviceScanDatabaseTable.findScan(scanId)
      .thenCompose(scan -> decideDeviceScan(user, scan, approved));
  }

  private CompletableFuture<Map<String, Object>> decideDeviceScan(
    User user, DeviceScan scan, boolean approved
  ) {
    if (!scan.creatorId().equals(user.id())) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1001));
    }
    if (!scan.scanned()) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1002));
    }
    if (!approved) {
      return deviceScanDatabaseTable.deleteScan(scan.id())
        .thenApply(value -> Map.of("success", true));
    }
    return deviceScanDatabaseTable.approveScan(scan)
      .thenApply(value -> Map.of("success", true));
  }

  @RequestMapping(path = "/device/scan/scanned/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> wasDeviceScanned(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var scanId = body.getUUID("scan");
    return findUser(request)
      .thenCompose(user -> deviceScanDatabaseTable.scanExists(scanId)
        .thenCompose(exists -> wasDeviceScanned(user, scanId, exists)));
  }

  private CompletableFuture<Map<String, Object>> wasDeviceScanned(
    User user, UUID scanId, boolean scanExists
  ) {
    if (!scanExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1000));
    }
    return deviceScanDatabaseTable.findScan(scanId)
      .thenApply(scan -> wasDeviceScanned(user, scan));
  }

  private Map<String, Object> wasDeviceScanned(
    User user, DeviceScan scan
  ) {
    if (!scan.creatorId().equals(user.id())) {
      return Map.of("success", false, "errorCode", 1001);
    }
    if (scan.approved()) {
      return Map.of("success", false, "errorCode", 1002);
    }
    if (!scan.scanned()) {
      return Map.of("success", true, "scanned", false);
    }
    return Map.of("success", true, "scanned", true, "information",
      scan.information(), "platform", scan.platform());
  }

  @RequestMapping(path = "/device/scan/approved/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> isScanApproved(
    HttpServletRequest request,
    @RequestBody String payload, HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var scanId = body.getUUID("scan");
    var token = body.getUUID("token");
    return deviceScanDatabaseTable.scanExists(scanId)
      .thenCompose(exists -> isScanApproved(request, scanId, token, exists));
  }

  private CompletableFuture<Map<String, Object>> isScanApproved(
    HttpServletRequest request, UUID scanId, UUID token, boolean scanExists
  ) {
    if (!scanExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1000));
    }
    return deviceScanDatabaseTable.findScan(scanId)
      .thenCompose(scan -> isScanApproved(request, scan, token));
  }

  private CompletableFuture<Map<String, Object>> isScanApproved(
    HttpServletRequest request, DeviceScan scan, UUID token
  ) {
    if (!scan.token().equals(token)) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1001));
    }
    if (!scan.scanned()) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "errorCode", 1002));
    }
    if (!scan.approved()) {
      return CompletableFuture.completedFuture(Map.of("success", true,
        "approved", false));
    }
    return userDatabaseTable().findUser(scan.creatorId())
      .thenCompose(creator -> isScanApproved(request, scan, creator));
  }

  private CompletableFuture<Map<String, Object>> isScanApproved(
    HttpServletRequest request, DeviceScan scan, User creator
  ) {
    var verification = Verification.create(userDatabaseTable(), homeKey,
      secretKey(), refreshKey, hashing, creator.email(), "");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    verificationLoginController.processAuthorizedLogin(request, verification,
      futureResponse);
    return futureResponse.thenCompose(loginResult ->
      isScanApproved(scan, creator, loginResult));
  }

  private CompletableFuture<Map<String, Object>> isScanApproved(
    DeviceScan scan, User creator, Map<String, Object> loginResult
  ) {
    if (!((boolean) loginResult.get("success"))) {
      return CompletableFuture.completedFuture(
        Map.of("success", false, "errorCode", 1003));
    }
    return deviceDatabaseTable().deviceExists(scan.machine(), scan.creatorId())
      .thenCompose(exists -> deviceModificationController.deviceLogin(creator,
          scan.machine(), scan.information(),
          DevicePlatform.valueOf(scan.platform().toUpperCase()),
          scan.firebaseToken(), exists)
        .thenApply(deviceResult -> isScanApproved(scan, deviceResult, loginResult)));
  }

  private Map<String, Object> isScanApproved(
    DeviceScan scan, Map<String, Object> deviceResult,
    Map<String, Object> loginResult
  ) {
    deviceScanDatabaseTable.deleteScan(scan.id());
    if (!scan.targetId().equals(scan.creatorId())) {
      var deviceId = (String) deviceResult.get("id");
      userDeviceDatabaseTable.userHasDevice(scan.targetId(), deviceId)
        .thenAccept(isTrusted -> trustScanTarget(deviceId, scan, isTrusted));
    }
    var information = Maps.<String, Object>newHashMap();
    information.putAll(deviceResult);
    information.putAll(loginResult);
    information.put("success", true);
    information.put("approved", true);
    return information;
  }

  private void trustScanTarget(String deviceId, DeviceScan scan, boolean isTrusted) {
    if (isTrusted) {
      return;
    }
    deviceDatabaseTable().findDevice(deviceId).thenAccept(device ->
      userDeviceDatabaseTable.insertUserDevice(scan.targetId(), device.id(),
        device.ownerId(), device.information(), device.platform()));
  }
}
