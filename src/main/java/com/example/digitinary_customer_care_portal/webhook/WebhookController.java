package com.example.digitinary_customer_care_portal.webhook;

import com.example.digitinary_customer_care_portal.dto.webhook.SubscribeRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<WebhookSubscription> subscribe(@Valid @RequestBody SubscribeRequest req) {
        WebhookSubscription sub = webhookService.subscribe(req.url(), req.eventType());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(sub.id())
                .toUri();

        return ResponseEntity.created(location).body(sub);
    }

    @PostMapping("/incoming/orders")
    public ResponseEntity<Void> receiveOrderEvent(
            @RequestHeader("X-Digitinary-Signature") String signature,
            @RequestBody String body) {

        boolean valid = webhookService.verifySignature(body, signature);
        log.info("Webhook received | signatureValid={} | signature={}", valid, signature);
        log.info("Webhook body: {}", body);

        return ResponseEntity.ok().build();
    }
}
