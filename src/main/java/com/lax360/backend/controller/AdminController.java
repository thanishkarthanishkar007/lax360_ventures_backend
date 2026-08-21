package com.lax360.backend.controller;

import com.lax360.backend.model.WebsiteContent;
import com.lax360.backend.repository.WebsiteContentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String DEFAULT_PASSCODE = "lax360@1234";
    private final WebsiteContentRepository contentRepository;

    public AdminController(WebsiteContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    private String getStoredPasscode() {
        return contentRepository.findByKey("admin_passcode")
                .map(WebsiteContent::getContent)
                .filter(p -> p != null && !p.isBlank())
                .orElse(DEFAULT_PASSCODE);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPasscode(@RequestBody Map<String, String> body) {
        String passcode = body.get("passcode");
        String currentPasscode = getStoredPasscode();

        if (currentPasscode.equals(passcode)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Authenticated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid passcode"));
        }
    }

    @PostMapping("/change-passcode")
    public ResponseEntity<Map<String, Object>> changePasscode(@RequestBody Map<String, String> body) {
        String currentPasscode = body.get("currentPasscode");
        String newPasscode = body.get("newPasscode");

        if (newPasscode == null || newPasscode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "New passcode cannot be empty"));
        }

        String storedPasscode = getStoredPasscode();
        if (!storedPasscode.equals(currentPasscode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Current passcode is incorrect"));
        }

        WebsiteContent item = contentRepository.findByKey("admin_passcode")
                .orElse(new WebsiteContent("admin_passcode", "Admin Passcode", DEFAULT_PASSCODE));

        item.setContent(newPasscode.trim());
        item.setUpdatedAt(Instant.now());
        contentRepository.save(item);

        return ResponseEntity.ok(Map.of("success", true, "message", "Admin passcode updated successfully"));
    }
}
