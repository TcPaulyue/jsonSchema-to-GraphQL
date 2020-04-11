package com.sddm.querybuilder.repository;

import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.domain.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemaRepository extends MongoRepository<Schema, String> {
    List<Schema> findAllByStatus(Status status);
}