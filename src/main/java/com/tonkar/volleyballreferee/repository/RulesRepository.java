package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RulesRepository extends MongoRepository<Rules, UUID> {

    List<Rules> findByCreatedByOrderByNameAsc(String userId);

    Optional<Rules> findByIdAndCreatedBy(UUID id, String userId);

    Optional<Rules> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind);

    boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);
}
