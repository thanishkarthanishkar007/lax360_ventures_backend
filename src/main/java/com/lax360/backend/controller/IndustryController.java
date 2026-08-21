package com.lax360.backend.controller;

import com.lax360.backend.model.Industry;
import com.lax360.backend.repository.IndustryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/industries")
public class IndustryController {

    private final IndustryRepository repository;

    public IndustryController(IndustryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Industry> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Industry create(@RequestBody Industry industry) {
        if (industry.getName() == null || industry.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Industry name is required");
        }
        return repository.save(industry);
    }

    @PutMapping("/{id}")
    public Industry update(@PathVariable String id, @RequestBody Industry industry) {
        Industry existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Industry not found"));
        existing.setName(industry.getName());
        existing.setDescription(industry.getDescription());
        existing.setIcon(industry.getIcon());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }
}
