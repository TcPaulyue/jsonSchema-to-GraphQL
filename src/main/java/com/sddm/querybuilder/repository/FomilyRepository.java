package com.sddm.querybuilder.repository;

import com.sddm.querybuilder.domain.Fomily;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FomilyRepository extends MongoRepository<Fomily,String> {
}
