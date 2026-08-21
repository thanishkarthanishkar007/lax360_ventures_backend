package com.lax360.backend.controller;

import com.lax360.backend.model.TeamMember;
import com.lax360.backend.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamMemberController {

    private final TeamMemberRepository repository;

    public TeamMemberController(TeamMemberRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TeamMember> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMember create(@RequestBody TeamMember member) {
        if (member.getName() == null || member.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        return repository.save(member);
    }

    @PutMapping("/{id}")
    public TeamMember update(@PathVariable String id, @RequestBody TeamMember member) {
        TeamMember existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found"));
        existing.setName(member.getName());
        existing.setRole(member.getRole());
        existing.setInitials(member.getInitials());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }
}
