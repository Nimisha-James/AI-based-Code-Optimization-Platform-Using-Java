package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.demo.model.CodeEntry;

public interface CodeRepository extends MongoRepository<CodeEntry, String> {
}
