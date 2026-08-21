package com.lax360.backend.service;

import com.lax360.backend.dto.DemoRequestDto;
import com.lax360.backend.model.DemoRequest;
import com.lax360.backend.repository.DemoRequestRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoRequestService {

    private final DemoRequestRepository repository;
    private final EmailService emailService;

    public DemoRequestService(DemoRequestRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public DemoRequest createDemoRequest(DemoRequestDto dto) {
        DemoRequest request = new DemoRequest(
                dto.getFullName().trim(),
                dto.getEmail().trim(),
                dto.getMobileNumber().trim(),
                dto.getCompany().trim(),
                dto.getProduct().trim(),
                dto.getCustomRequirement() != null ? dto.getCustomRequirement().trim() : null
        );

        DemoRequest saved = repository.save(request);
        emailService.sendDemoRequestNotification(saved);
        return saved;
    }

    public List<DemoRequest> getAllDemoRequests() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public void deleteDemoRequest(String id) {
        repository.deleteById(id);
    }
}
