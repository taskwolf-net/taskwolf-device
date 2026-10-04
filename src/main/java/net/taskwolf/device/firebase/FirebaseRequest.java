package net.taskwolf.device.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import net.taskwolf.device.DeviceConfiguration;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class FirebaseRequest {
  private final DeviceConfiguration deviceConfiguration;
  private final GoogleCredentials googleCredentials;
  private final String receiver;

  private static final String FIREBASE_URL =
    "https://fcm.googleapis.com/v1/projects/%s/messages:send";

  public void send(String key, Map<String, Object> payload) {
    try {
      googleCredentials.refreshIfExpired();
      var token = googleCredentials.getAccessToken().getTokenValue();
      var url = String.format(FIREBASE_URL, deviceConfiguration.firebaseProjectId());
      var requestBody = new JSONObject(Map.of("message",
        Map.of("token", receiver, key, payload)));
      var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .setHeader("Content-Type", "application/json")
        .setHeader("Authorization", "Bearer " + token)
        .build();
      HttpClient.newHttpClient().sendAsync(requestBuilder,
        HttpResponse.BodyHandlers.ofByteArray());
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
