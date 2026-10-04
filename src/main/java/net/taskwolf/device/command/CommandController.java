package net.taskwolf.device.command;

import net.taskwolf.device.access.DeviceController;
import net.taskwolf.device.structure.Device;
import net.taskwolf.device.structure.DeviceDatabaseTable;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRequestBody;
import net.taskwolf.core.organization.team.TeamDatabaseTable;
import net.taskwolf.core.organization.team.TeamTargetDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.user.UserTargetDatabaseTable;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.device.distribution.command.packet.outgoing.PacketOutgoingCommandResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class CommandController extends DeviceController {
  private final CommandExecutionDatabaseTable commandExecutionDatabaseTable;
  private final WorkerProxyClient workerProxyClient;

  private CommandController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    DeviceDatabaseTable deviceDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    CommandExecutionDatabaseTable commandExecutionDatabaseTable,
    WorkerProxyClient workerProxyClient
  ) {
    super(secretKey, userDatabaseTable, deviceDatabaseTable,
      userTargetDatabaseTable, teamTargetDatabaseTable, teamDatabaseTable);
    this.commandExecutionDatabaseTable = commandExecutionDatabaseTable;
    this.workerProxyClient = workerProxyClient;
  }

  @RequestMapping(path = "/device/command/settings/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findCommandSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> futureResponse.complete(Map.of("commandExecution",
        device.commandExecution())),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  @RequestMapping(path = "/device/command/settings/update/", method = RequestMethod.POST)
  public void updateCommandSettings(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId, device ->
      deviceDatabaseTable().updateDeviceCommandSettings(device,
        body.getBoolean("commandExecution")), () -> {});
  }

  @RequestMapping(path = "/device/command/history/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> findCommandHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    performDeviceOperation(findUserId(request), deviceId,
      device -> findCommandHistory(device).thenAccept(futureResponse::complete),
      () -> futureResponse.complete(Maps.newHashMap()));
    return futureResponse;
  }

  private CompletableFuture<Map<String, Object>> findCommandHistory(
    Device device
  ) {
    return commandExecutionDatabaseTable.findExecutionsOfDevice(device.id())
      .thenApply(executions -> executions.stream().sorted(
        Comparator.comparing(CommandExecution::created).reversed()).toList())
      .thenApply(executions -> Map.of("history", executions.stream()
        .map(this::assemblyCommandInformation).toList()));
  }

  private Map<String, Object> assemblyCommandInformation(
    CommandExecution execution
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("command", execution.command());
    information.put("output", execution.output());
    information.put("errorMessage", execution.errorMessage());
    information.put("exitCode", execution.exitCode());
    information.put("time", formatTime(execution.created()));
    return information;
  }

  @RequestMapping(path = "/device/command/history/reset/", method = RequestMethod.POST)
  public void resetCommandHistory(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    var deviceId = body.getString("device");
    performDeviceOperation(findUserId(request), deviceId,
      this::resetCommandHistory, () -> {});
  }

  private void resetCommandHistory(Device device) {
    commandExecutionDatabaseTable.findExecutionsOfDevice(device.id())
      .thenAccept(executions -> executions.forEach(execution ->
        commandExecutionDatabaseTable.deleteCommandExecution(execution.id())));
  }

  @RequestMapping(path = "/device/command/response/", method = RequestMethod.POST)
  public void deviceCommandResponse(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = TaskwolfRequestBody.of(payload, response);
    performDeviceOperation(findUserId(request), body.getString("device"),
      device -> deviceCommandResponse(body.getUUID("command"),
        body.getString("output"), body.getString("errorMessage"),
        body.getInt("exitCode")), () -> {});
  }

  private void deviceCommandResponse(
    UUID command, String output, String errorMessage, int exitCode
  ) {
    workerProxyClient.sendPacket(new PacketOutgoingCommandResponse(command,
      true, output, errorMessage, exitCode));
  }

  private String formatTime(long time) {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
