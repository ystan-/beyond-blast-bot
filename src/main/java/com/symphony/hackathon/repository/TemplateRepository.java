package com.symphony.hackathon.repository;

import com.symphony.hackathon.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TemplateRepository extends MongoRepository<Template, Long> {
}
