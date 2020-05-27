package com.symphony.hackathon.repository;

import com.symphony.hackathon.model.DistributionList;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DistributionListRepository extends MongoRepository<DistributionList, Long> {
}
