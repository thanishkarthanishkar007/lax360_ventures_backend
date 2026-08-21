package com.lax360.backend.controller;

import com.lax360.backend.model.WebsiteContent;
import com.lax360.backend.repository.WebsiteContentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/content")
public class WebsiteContentController {

    private final WebsiteContentRepository repository;

    public WebsiteContentController(WebsiteContentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<WebsiteContent> getAll() {
        return repository.findAll();
    }

    @PutMapping("/{key}")
    public WebsiteContent saveOrUpdate(@PathVariable String key, @RequestBody WebsiteContent payload) {
        WebsiteContent item = repository.findByKey(key).orElse(new WebsiteContent());
        item.setKey(key);
        item.setTitle(payload.getTitle());
        item.setContent(payload.getContent());
        item.setUpdatedAt(Instant.now());
        return repository.save(item);
    }
}
