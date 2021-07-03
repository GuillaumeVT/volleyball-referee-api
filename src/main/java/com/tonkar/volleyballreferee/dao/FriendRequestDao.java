package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.entity.FriendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FriendRequestDao {

    private final MongoTemplate mongoTemplate;

    public void save(FriendRequest friendRequest) {
        mongoTemplate.save(friendRequest);
    }

    public long countByReceiverId(String receiverId) {
        Query query = Query.query(Criteria.where("receiverId").is(receiverId));
        return mongoTemplate.count(query, FriendRequest.class);
    }

    public boolean existsBySenderIdAndReceiverId(String senderId, String receiverId) {
        Query query = Query.query(Criteria.where("senderId").is(senderId).and("receiverId").is(receiverId));
        return mongoTemplate.exists(query, FriendRequest.class);
    }

    public Optional<FriendRequest> findByIdAndReceiverId(UUID id, String receiverId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("receiverId").is(receiverId));
        return Optional.ofNullable(mongoTemplate.findOne(query, FriendRequest.class));
    }

    public List<FriendRequest> findBySenderId(String senderId) {
        Query query = Query.query(Criteria.where("senderId").is(senderId));
        return mongoTemplate.find(query, FriendRequest.class);
    }

    public List<FriendRequest> findByReceiverId(String receiverId) {
        Query query = Query.query(Criteria.where("receiverId").is(receiverId));
        return mongoTemplate.find(query, FriendRequest.class);
    }

    public void deleteByIdAndReceiverId(UUID id, String receiverId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("receiverId").is(receiverId));
        mongoTemplate.remove(query, FriendRequest.class);
    }

    public void deleteById(FriendRequest friendRequest) {
        mongoTemplate.remove(friendRequest);
    }
}
