package com.lax360.backend.service;

import com.lax360.backend.model.DemoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Sends a notification email via the Resend HTTP API whenever a new demo
 * request comes in. The API key, sender identity, and notify-to address
 * are all injected from environment variables — nothing is hardcoded.
 *
 * A failure to send email never fails the request: the lead is already
 * safely stored in MongoDB by the time this runs, so email is best-effort.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Value("${resend.notify-email}")
    private String notifyEmail;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Runs on a background thread (see AsyncConfig) so the controller can
     * respond to the frontend as soon as MongoDB save succeeds, instead of
     * waiting on the Resend HTTP round-trip too. This is what previously
     * made every demo request submission block for however long the
     * Resend call (or Render cold start / DNS / TLS handshake) took.
     */
    @Async("emailTaskExecutor")
    public void sendDemoRequestNotification(DemoRequest req) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set — skipping email notification for {}", req.getEmail());
            return;
        }

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(notifyEmail),
                "subject", "New Demo Request - " + req.getFullName() + " (" + req.getCompany() + ")",
                "html", buildHtml(req)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, entity, String.class);
            log.info("Resend notification sent for demoRequestId={} status={}", req.getId(), response.getStatusCode());
        } catch (RestClientException ex) {
            log.error("Failed to send Resend email notification for demoRequestId={}: {}", req.getId(), ex.getMessage());
        }
    }

    private String buildHtml(DemoRequest req) {
        String submittedAt = req.getCreatedAt()
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'"));

        String customRow = (req.getCustomRequirement() != null && !req.getCustomRequirement().isBlank())
                ? row("Custom Requirement", req.getCustomRequirement())
                : "";

        return "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:0 auto;\">"
                + "<h2 style=\"color:#6D28D9;margin-bottom:16px;\">New Book-a-Demo Request</h2>"
                + "<table style=\"width:100%;border-collapse:collapse;font-size:14px;\">"
                + row("Full name", req.getFullName())
                + row("Work email", req.getEmail())
                + row("Mobile number", req.getMobileNumber())
                + row("Company", req.getCompany())
                + row("Interested in", req.getProduct())
                + customRow
                + row("Submitted at", submittedAt)
                + "</table>"
                + "<p style=\"margin-top:24px;color:#888;font-size:12px;\">"
                + "Sent automatically by the LAX360 Ventures website.</p>"
                + "</div>";
    }

    private String row(String label, String value) {
        return "<tr>"
                + "<td style=\"padding:6px 12px;color:#666;border-bottom:1px solid #eee;\">" + label + "</td>"
                + "<td style=\"padding:6px 12px;font-weight:600;border-bottom:1px solid #eee;\">" + escape(value) + "</td>"
                + "</tr>";
    }

    public String getNotifyEmail() {
        return (notifyEmail != null && !notifyEmail.isBlank()) ? notifyEmail.trim() : "lax360pr.ltd@gmail.com";
    }

    public boolean sendForgotPasscodeEmail(String passcode) {
        String recipient = getNotifyEmail();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set — cannot send forgot passcode email");
            return false;
        }

        String html = "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:0 auto;padding:20px;border:1px solid #eee;border-radius:12px;\">"
                + "<h2 style=\"color:#6D28D9;margin-bottom:16px;\">LAX360 Admin Passcode Recovery</h2>"
                + "<p style=\"font-size:14px;color:#333;\">You requested your LAX360 Ventures Admin Panel passcode.</p>"
                + "<div style=\"background:#F3E8FF;padding:16px;border-radius:8px;text-align:center;margin:20px 0;\">"
                + "<span style=\"font-size:12px;color:#6B21A8;display:block;margin-bottom:4px;font-weight:bold;\">YOUR CURRENT ADMIN PASSCODE:</span>"
                + "<span style=\"font-size:22px;font-weight:bold;color:#4C1D95;letter-spacing:2px;\">" + escape(passcode) + "</span>"
                + "</div>"
                + "<p style=\"font-size:12px;color:#888;\">If you did not request this email, please secure your admin account.</p>"
                + "</div>";

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(recipient),
                "subject", "LAX360 Ventures - Admin Passcode Recovery",
                "html", html
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, entity, String.class);
            log.info("Resend forgot passcode email sent to {} status={}", recipient, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            log.error("Failed to send forgot passcode email to {}: {}", recipient, ex.getMessage());
            return false;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
