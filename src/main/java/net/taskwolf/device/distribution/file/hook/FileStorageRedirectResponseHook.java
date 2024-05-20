package net.taskwolf.device.distribution.file.hook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.whitelist.WhitelistConfiguration;
import net.taskwolf.device.distribution.file.event.WorkerFileStorageRedirectResponseEvent;
import net.taskwolf.device.file.storage.FileStorageRedirectRepository;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileStorageRedirectResponseHook implements Hook {
  private final FileStorageRedirectRepository fileStorageRedirectRepository;
  private final WhitelistConfiguration whitelistConfiguration;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @EventHook
  private void fileStorageRedirectResponse(
    WorkerFileStorageRedirectResponseEvent event
  ) {
    var storageId = event.storageId();
    var redirectContentOptional =
      fileStorageRedirectRepository.findRedirectContent(storageId);
    if (redirectContentOptional.isEmpty()) {
      return;
    }
    var redirectContent = redirectContentOptional.get();
    var payload = new JSONObject(Map.of("device", redirectContent.deviceId(),
      "storage", storageId)).toString();
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(event.redirectUrl()))
      .method("POST", HttpRequest.BodyPublishers.ofString(payload));
    if (whitelistConfiguration.whitelistEnabled()) {
      requestBuilder.setHeader("WHITELIST-KEY", whitelistConfiguration.whitelistKey());
    }
    requestBuilder.setHeader("Authorization", "Bearer " + redirectContent.apiKey());
    var httpRequest = requestBuilder.build();
    httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
      .thenAccept(response -> createResponseEntity(response, storageId,
        redirectContent.futureResponse()));
  }

  private void createResponseEntity(
    HttpResponse<String> httpResponse, UUID storageId,
    CompletableFuture<Map<String, Object>> futureResponse
  ) {
    var result = new JSONObject(httpResponse.body()).toMap();
    futureResponse.complete(result);
    fileStorageRedirectRepository.unregisterStorageRedirect(storageId);
  }
}
