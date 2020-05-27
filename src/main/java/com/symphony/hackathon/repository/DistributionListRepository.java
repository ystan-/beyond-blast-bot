package com.symphony.hackathon.repository;

import com.symphony.hackathon.model.DistributionList;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DistributionListRepository extends MongoRepository<DistributionList, Long> {
    List<DistributionList> findAllByOwner(long owner);
}
