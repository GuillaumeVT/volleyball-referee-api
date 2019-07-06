package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.User;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByPseudo(String pseudo);

    boolean existsByEmail(String email);

    Optional<User> findByPseudo(String pseudo);

    Optional<User> findByEmail(String email);

    @ExistsQuery("{ 'id': ?0, 'friends.id': ?1 }")
    boolean areFriends(String id1, String id2);

    boolean existsByPurchaseToken(String purchaseToken);

    @Query("{ 'friends.id': ?0 }")
    Stream<User> findByFriend(String friendId);
}
