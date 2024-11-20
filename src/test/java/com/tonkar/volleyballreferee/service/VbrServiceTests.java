package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.entity.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
class VbrServiceTests {

    @Autowired
    protected VbrTestConfiguration.Sandbox sandbox;

    @Autowired
    protected Faker faker;

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3").withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setUp(@Autowired MongoTemplate mongoTemplate) {
        if (mongoTemplate.collectionExists(User.class)) {
            mongoTemplate.dropCollection(User.class);
        }
        if (mongoTemplate.collectionExists(FriendRequest.class)) {
            mongoTemplate.dropCollection(FriendRequest.class);
        }
        if (mongoTemplate.collectionExists(Rules.class)) {
            mongoTemplate.dropCollection(Rules.class);
        }
        if (mongoTemplate.collectionExists(Team.class)) {
            mongoTemplate.dropCollection(Team.class);
        }
        if (mongoTemplate.collectionExists(League.class)) {
            mongoTemplate.dropCollection(League.class);
        }
        if (mongoTemplate.collectionExists(Game.class)) {
            mongoTemplate.dropCollection(Game.class);
        }
    }
}
