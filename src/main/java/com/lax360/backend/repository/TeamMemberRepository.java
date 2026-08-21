package com.lax360.backend.repository;

import com.lax360.backend.model.TeamMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends MongoRepository<TeamMember, String> {
}
