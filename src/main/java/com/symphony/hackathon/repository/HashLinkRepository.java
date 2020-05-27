package com.symphony.hackathon.repository;

import com.symphony.hackathon.model.HashLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HashLinkRepository extends MongoRepository<HashLink, String> {}
