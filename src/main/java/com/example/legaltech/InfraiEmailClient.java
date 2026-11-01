package com.example.legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiEmailClient {
    // Canonical call shape: infrai.email.send
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;

    public InfraiEmailClient(String baseUrl, String key) {
        this.baseUrl = baseUrl;
        this.key = key;
    }

    public String send(String to, String subject, String body, String requestId) throws IOException, InterruptedException {
        String payload = "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject)
                + "\",\"body\":\"" + json(body) + "\"}";
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/email/send"))
                    .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").header("Idempotency-Key", requestId)
                    .method("POST", HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true")) throw new IOException("Infrai email rejected: " + envelope);
            if (response.statusCode() == 429 && attempt < 2) {
                Thread.sleep(retryDelay(response, attempt));
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("Email transport failed: " + response.statusCode());
            return messageId(envelope);
        }
        throw new IOException("Email retry budget exhausted");
    }

    private long retryDelay(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt) * 1000L;
    }

    private String messageId(String envelope) {
        int start = envelope.indexOf("\"message_id\":\"") + 15;
        int end = envelope.indexOf('"', start);
        return start >= 15 && end > start ? envelope.substring(start, end) : "accepted";
    }

    private String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
