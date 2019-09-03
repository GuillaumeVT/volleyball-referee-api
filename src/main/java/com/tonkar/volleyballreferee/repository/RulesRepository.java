package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface RulesRepository extends MongoRepository<Rules, UUID> {

    Stream<Rules> findByCreatedByOrderByNameAsc(String userId);

    Optional<Rules> findByIdAndCreatedBy(UUID id, String userId);

    Optional<Rules> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind);

    boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind);

    long countByCreatedBy(String userId);

    void deleteByIdAndCreatedBy(UUID id, String userId);

    void deleteByCreatedBy(String userId);
}
