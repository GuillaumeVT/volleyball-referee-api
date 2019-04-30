package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesDescription;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class RulesTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ParameterizedTypeReference<List<RulesDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<RulesDescription>> getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesDescrResponse.getStatusCode());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesDescrResponse.getStatusCode());

        ResponseEntity<Rules> getRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Rules.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesResponse.getStatusCode());

        ResponseEntity<RulesDescription> getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/default/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), RulesDescription.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getDefaultRulesResponse.getStatusCode());

        ResponseEntity<Count> getRulesCountResponse = restTemplate.exchange(urlOf("/api/v3/rules/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesCountResponse.getStatusCode());

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUserInvalidAuth, Rules.OFFICIAL_INDOOR_RULES), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.PUT, payloadWithAuth(testUserInvalidAuth, Rules.OFFICIAL_INDOOR_RULES), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidAuth), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());
    }

    @Test
    public void testManageRules() {
        Rules rules = new Rules(UUID.randomUUID(), testUser1Id, 0L, 0L, "Test rules", GameType.INDOOR,
                5, 25, true, 15, true, true, true, 2, 30,
                true, 60, true, 180,
                Rules.NO_LIMITATION, 6, false, 0, 0, 9999);
        UUID rulesId = rules.getId();

        User user = new User();
        user.setId(testUser1Id);
        user.setPseudo("VBR1");
        user.setFriends(new ArrayList<>());

        // Create user

        ResponseEntity<String> postUserResponse = restTemplate.exchange(urlOf(String.format("/api/v3/public/users/%s", vbrSignUpKey)), HttpMethod.POST, payloadWithoutAuth(user), String.class);
        assertEquals(HttpStatus.CREATED, postUserResponse.getStatusCode());

        // Rules don't exist yet

        ResponseEntity<Rules> getRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + rulesId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Rules.class);
        assertEquals(HttpStatus.NOT_FOUND, getRulesResponse.getStatusCode());

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.NOT_FOUND, rulesResponse.getStatusCode());

        // Default rules

        ResponseEntity<RulesDescription> getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/default/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), RulesDescription.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/default/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), RulesDescription.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/default/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), RulesDescription.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        // Create rules

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        // Rules already exist

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());


        rules.setId(UUID.randomUUID());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.POST, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());

        rules.setId(rulesId);

        // Update rules

        rules.setUpdatedAt(System.currentTimeMillis());
        rules.setCustomConsecutiveServesPerPlayer(10);
        rules.setBeachCourtSwitches(true);
        rules.setGameIntervalDuration(10);

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.PUT, payloadWithAuth(testUser1Auth, rules), String.class);
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());

        // Count rules

        ResponseEntity<Count> getRulesCountResponse = restTemplate.exchange(urlOf("/api/v3/rules/count"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Count.class);
        assertEquals(HttpStatus.OK, getRulesCountResponse.getStatusCode());
        assertEquals(1L, getRulesCountResponse.getBody().getCount());

        // Get rules

        getRulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + rulesId), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Rules.class);
        assertEquals(HttpStatus.OK, getRulesResponse.getStatusCode());
        assertEquals(rules.getName(), getRulesResponse.getBody().getName());

        // List all rules

        ParameterizedTypeReference<List<RulesDescription>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<RulesDescription>> getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        // List all rules of kind

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());

        // Delete rules

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules/" + rulesId), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.DELETE, emptyPayloadWithAuth(testUser1Auth), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());
    }
}
