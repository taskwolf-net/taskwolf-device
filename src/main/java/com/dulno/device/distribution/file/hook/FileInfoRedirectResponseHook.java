package com.dulno.device.distribution.file.hook;

import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.whitelist.WhitelistConfiguration;
import com.dulno.device.distribution.file.event.WorkerFileInfoRedirectResponseEvent;
import com.dulno.device.file.info.FileInfoRedirectRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class FileInfoRedirectResponseHook implements Hook {
  private final FileInfoRedirectRepository fileInfoRedirectRepository;
  private final WhitelistConfiguration whitelistConfiguration;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @EventHook
  private void fileInfoRedirectResponse(
    WorkerFileInfoRedirectResponseEvent event
  ) {
    var infoId = event.infoId();
    var redirectContentOptional =
      fileInfoRedirectRepository.findRedirectContent(infoId);
    if (redirectContentOptional.isEmpty()) {
      return;
    }
    var redirectContent = redirectContentOptional.get();
    var payload = new JSONObject(Map.of("device", redirectContent.deviceId(),
      "info", infoId, "content", redirectContent.content())).toString();
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(event.redirectUrl()))
      .method("POST", HttpRequest.BodyPublishers.ofString(payload));
    if (whitelistConfiguration.whitelistEnabled()) {
      requestBuilder.setHeader("WHITELIST-KEY", whitelistConfiguration.whitelistKey());
    }
    requestBuilder.setHeader("Authorization", "Bearer " + redirectContent.apiKey());
    var httpRequest = requestBuilder.build();
    httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    fileInfoRedirectRepository.unregisterInfoRedirect(infoId);
  }
}
