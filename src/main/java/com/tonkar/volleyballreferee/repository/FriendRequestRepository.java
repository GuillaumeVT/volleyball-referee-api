package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, UUID> {

    long countByReceiverId(String receiverId);

    boolean existsBySenderIdAndReceiverId(String senderId, String receiverId);

    Optional<FriendRequest> findByIdAndReceiverId(UUID id, String receiverId);

    List<FriendRequest> findBySenderId(String senderId);

    List<FriendRequest> findByReceiverId(String receiverId);

    void deleteByIdAndReceiverId(UUID id, String receiverId);

}
