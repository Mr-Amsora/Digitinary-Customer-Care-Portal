package com.example.digitinary_customer_care_portal.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final Map<String, WebhookSubscription> subscriptions = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookService(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          @Value("${webhook.secret}") String webhookSecret) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }


    public WebhookSubscription subscribe(String url, String eventType) {
        String id = UUID.randomUUID().toString();
        WebhookSubscription sub = new WebhookSubscription(id, url, eventType);
        subscriptions.put(id, sub);
        log.info("Webhook subscribed: id={}, url={}, eventType={}", id, url, eventType);
        return sub;
    }


    @Async
    public void deliver(String eventType, Object payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            String signature = computeHmac(body);

            subscriptions.values().stream()
                    .filter(s -> s.eventType().equals(eventType))
                    .forEach(s -> post(s.url(), body, signature));

        } catch (Exception e) {
            log.error("Failed to serialize webhook payload for event={}", eventType, e);
        }
    }


    public boolean verifySignature(String body, String incomingSignature) {
        try {
            String expected = computeHmac(body);
            return expected.equals(incomingSignature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }


    private void post(String url, String body, String signature) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Digitinary-Signature", signature);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Webhook delivered successfully to: {}", url);

        } catch (Exception e) {
            log.error("Webhook delivery failed for url={}: {}", url, e.getMessage());
        }
    }

    private String computeHmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(raw);
    }
}
