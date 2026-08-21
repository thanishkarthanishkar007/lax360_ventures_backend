package com.lax360.backend.repository;

import com.lax360.backend.model.WebsiteContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteContentRepository extends MongoRepository<WebsiteContent, String> {
    Optional<WebsiteContent> findByKey(String key);
}
