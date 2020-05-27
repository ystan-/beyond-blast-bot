package com.symphony.hackathon.repository;

import com.symphony.hackathon.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TemplateRepository extends MongoRepository<Template, Long> {
    List<Template> findAllByOwner(long owner);
}
