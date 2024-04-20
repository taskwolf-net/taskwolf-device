package net.taskwolf.device.firebase;

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
  private final String receiver;

  private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

  public void send(String key, Map<String, Object> payload) {
    var requestBody = new JSONObject(Map.of("to", receiver, key, payload));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(FIREBASE_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "key=" + deviceConfiguration.firebaseToken())
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }
}
