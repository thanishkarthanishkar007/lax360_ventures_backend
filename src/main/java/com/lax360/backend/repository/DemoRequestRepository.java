package com.lax360.backend.repository;

import com.lax360.backend.model.DemoRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRequestRepository extends MongoRepository<DemoRequest, String> {
}
