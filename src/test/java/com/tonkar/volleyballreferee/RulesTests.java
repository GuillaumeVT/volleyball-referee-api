package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class RulesTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ParameterizedTypeReference<List<RulesSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<RulesSummary>> getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesDescrResponse.getStatusCode());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), typeReference);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesDescrResponse.getStatusCode());

        ResponseEntity<Rules> getRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/" + UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Rules.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesResponse.getStatusCode());

        ResponseEntity<RulesSummary> getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/default/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), RulesSummary.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getDefaultRulesResponse.getStatusCode());

        ResponseEntity<Count> getRulesCountResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), Count.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getRulesCountResponse.getStatusCode());

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.POST, payloadWithAuth(testUserInvalidToken, Rules.OFFICIAL_INDOOR_RULES), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.PUT, payloadWithAuth(testUserInvalidToken, Rules.OFFICIAL_INDOOR_RULES), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/" + UUID.randomUUID()), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, rulesResponse.getStatusCode());
    }

    @Test
    public void testManageRules() {
        createUser1();

        Rules rules = new Rules(UUID.randomUUID(), testUser1.getId(), 0L, 0L, "Test rules", GameType.INDOOR,
                5, 25, true, 15, true, true, Rules.WIN_TERMINATION, true, 2, 30,
                true, 60, true, 180,
                Rules.NO_LIMITATION, 6, false, 0, 0, 9999);
        UUID rulesId = rules.getId();

        // Rules don't exist yet

        ResponseEntity<Rules> getRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/" + rulesId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Rules.class);
        assertEquals(HttpStatus.NOT_FOUND, getRulesResponse.getStatusCode());

        ResponseEntity<String> rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.PUT, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.NOT_FOUND, rulesResponse.getStatusCode());

        // Default rules

        ResponseEntity<RulesSummary> getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/default/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/default/kind/" + GameType.INDOOR_4X4), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/default/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        // Create rules

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.POST, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        // Rules already exist

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.POST, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());


        rules.setId(UUID.randomUUID());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.POST, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.CONFLICT, rulesResponse.getStatusCode());

        rules.setId(rulesId);

        // Update rules

        rules.setUpdatedAt(System.currentTimeMillis());
        rules.setCustomConsecutiveServesPerPlayer(10);
        rules.setBeachCourtSwitches(true);
        rules.setGameIntervalDuration(10);

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.PUT, payloadWithAuth(testUserToken1, rules), String.class);
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());

        // Count rules

        ResponseEntity<Count> getRulesCountResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/count"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getRulesCountResponse.getStatusCode());
        assertEquals(1L, getRulesCountResponse.getBody().getCount());

        // Get rules

        getRulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/" + rulesId), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Rules.class);
        assertEquals(HttpStatus.OK, getRulesResponse.getStatusCode());
        assertEquals(rules.getName(), getRulesResponse.getBody().getName());

        // List all rules

        ParameterizedTypeReference<List<RulesSummary>> typeReference = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<RulesSummary>> getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        // List all rules of kind

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/kind/" + GameType.INDOOR), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/kind/" + GameType.BEACH), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());

        // Delete rules

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules/" + rulesId), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), String.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        getRulesDescrResponse = restTemplate.exchange(urlOf("/api/v3.1/rules"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), typeReference);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());
    }
}
