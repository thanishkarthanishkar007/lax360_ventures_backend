package com.lax360.backend.controller;

import com.lax360.backend.dto.DemoRequestDto;
import com.lax360.backend.model.DemoRequest;
import com.lax360.backend.service.DemoRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demo-requests")
public class DemoRequestController {

    private final DemoRequestService service;

    public DemoRequestController(DemoRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoRequest submit(@Valid @RequestBody DemoRequestDto dto) {
        return service.createDemoRequest(dto);
    }

    /**
     * Simple listing endpoint for internal/admin use. It is intentionally
     * unauthenticated for now — put it behind Spring Security or an
     * API-key check before relying on it in production.
     */
    @GetMapping
    public List<DemoRequest> list() {
        return service.getAllDemoRequests();
    }
}
