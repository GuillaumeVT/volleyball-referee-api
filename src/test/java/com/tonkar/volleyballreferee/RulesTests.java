package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class RulesTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/rules").queryParam("kind", GameType.INDOOR);
        errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/" + UUID.randomUUID(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/default/kind/" + GameType.INDOOR, HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/count", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(testUserInvalidToken, Rules.OFFICIAL_INDOOR_RULES), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(testUserInvalidToken, Rules.OFFICIAL_INDOOR_RULES), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules/" + UUID.randomUUID(), HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.DELETE, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
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

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/rules/" + rulesId, HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(testUserToken1, rules), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());

        // Default rules

        ResponseEntity<RulesSummary> getDefaultRulesResponse = restTemplate.exchange("/rules/default/kind/" + GameType.INDOOR, HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange("/rules/default/kind/" + GameType.INDOOR_4X4, HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        getDefaultRulesResponse = restTemplate.exchange("/rules/default/kind/" + GameType.BEACH, HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), RulesSummary.class);
        assertEquals(HttpStatus.OK, getDefaultRulesResponse.getStatusCode());

        // Create rules

        ResponseEntity<Void> rulesResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(testUserToken1, rules), Void.class);
        assertEquals(HttpStatus.CREATED, rulesResponse.getStatusCode());

        // Rules already exist

        errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(testUserToken1, rules), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());


        rules.setId(UUID.randomUUID());

        errorResponse = restTemplate.exchange("/rules", HttpMethod.POST, payloadWithAuth(testUserToken1, rules), ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());

        rules.setId(rulesId);

        // Update rules

        rules.setUpdatedAt(System.currentTimeMillis());
        rules.setCustomConsecutiveServesPerPlayer(10);
        rules.setBeachCourtSwitches(true);
        rules.setGameIntervalDuration(10);

        rulesResponse = restTemplate.exchange("/rules", HttpMethod.PUT, payloadWithAuth(testUserToken1, rules), Void.class);
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());

        // Count rules

        ResponseEntity<Count> getRulesCountResponse = restTemplate.exchange("/rules/count", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Count.class);
        assertEquals(HttpStatus.OK, getRulesCountResponse.getStatusCode());
        assertEquals(1L, getRulesCountResponse.getBody().getCount());

        // Get rules

        ResponseEntity<Rules> getRulesResponse = restTemplate.exchange("/rules/" + rulesId, HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Rules.class);
        assertEquals(HttpStatus.OK, getRulesResponse.getStatusCode());
        assertEquals(rules.getName(), getRulesResponse.getBody().getName());

        // List all rules

        ParameterizedTypeReference<List<RulesSummary>> listType = new ParameterizedTypeReference<>() {};
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/rules");
        ResponseEntity<List<RulesSummary>> getRulesDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        // List all rules of kind

        uriBuilder = UriComponentsBuilder.fromUriString("/rules").queryParam("kind", GameType.INDOOR);
        getRulesDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        uriBuilder = UriComponentsBuilder.fromUriString("/rules").queryParam("kind", GameType.BEACH);
        getRulesDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());

        uriBuilder = UriComponentsBuilder.fromUriString("/rules").queryParam("kind", String.join(",", GameType.BEACH.toString(), GameType.INDOOR.toString()));
        getRulesDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(1, getRulesDescrResponse.getBody().size());

        // Delete rules

        rulesResponse = restTemplate.exchange("/rules/" + rulesId, HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        rulesResponse = restTemplate.exchange("/rules", HttpMethod.DELETE, emptyPayloadWithAuth(testUserToken1), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, rulesResponse.getStatusCode());

        uriBuilder = UriComponentsBuilder.fromUriString("/rules");
        getRulesDescrResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), listType);
        assertEquals(HttpStatus.OK, getRulesDescrResponse.getStatusCode());
        assertEquals(0, getRulesDescrResponse.getBody().size());
    }
}
