package com.sddm.querybuilder.repository;

import com.sddm.querybuilder.domain.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
}
