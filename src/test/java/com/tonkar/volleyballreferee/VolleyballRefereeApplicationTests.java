package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.model.*;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.service.MessageService;
import com.tonkar.volleyballreferee.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VolleyballRefereeApplicationTests {

	@Autowired
	private GameService gameService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private UserService userService;

	@LocalServerPort
	private int port;

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void testFromHttpToRepository() {
		// Add

		ResponseEntity<String> createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200793797"), HttpMethod.PUT, entityOf(game1), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200802314"), HttpMethod.PUT, entityOf(game2), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200804146"), HttpMethod.PUT, entityOf(game3), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200806997"), HttpMethod.PUT, entityOf(game4), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		// Update

		ResponseEntity<String> updateResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200793797"), HttpMethod.PUT, entityOf(game1), String.class);
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

		updateResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200802314"), HttpMethod.PUT, entityOf(game2), String.class);
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

		updateResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200804146"), HttpMethod.PUT, entityOf(game3), String.class);
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

		updateResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200806997"), HttpMethod.PUT, entityOf(game4), String.class);
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

		// Update Set

		updateResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200793797/set/1"), HttpMethod.PUT, entityOf(set2Game1), String.class);
		assertEquals(HttpStatus.OK, updateResponse.getStatusCode());


		// Is Synched

		ResponseEntity<Boolean> existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/123"), Boolean.class);
		assertEquals(false, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200793797"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200802314"), Boolean.class);
		assertEquals(false, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200804146"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200806997"), Boolean.class);
		assertEquals(false, existResponse.getBody());

		// Stats

		ResponseEntity<GameStatistics> statResponse = restTemplate.getForEntity(urlOf("/api/stats/game"), GameStatistics.class);
		assertEquals(4, statResponse.getBody().getGamesCount());
		assertEquals(2, statResponse.getBody().getLiveGamesCount());

		// Search

		ResponseEntity<GameDescription[]> searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/dummy"), GameDescription[].class);
		assertEquals(0, searchResponse.getBody().length);

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/bra"), GameDescription[].class);
		assertEquals(1, searchResponse.getBody().length);
		assertEquals(1516200793797L, searchResponse.getBody()[0].getDate());

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/live"), GameDescription[].class);
		assertEquals(2, searchResponse.getBody().length);
		assertEquals(1516200802314L, searchResponse.getBody()[0].getDate());
		assertEquals(1516200806997L, searchResponse.getBody()[1].getDate());

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/team"), GameDescription[].class);
		assertEquals(2, searchResponse.getBody().length);
		assertEquals(1516200804146L, searchResponse.getBody()[0].getDate());
		assertEquals(1516200806997L, searchResponse.getBody()[1].getDate());

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/vbr"), GameDescription[].class);
		assertEquals(4, searchResponse.getBody().length);

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/17-1-2018"), GameDescription[].class);
		assertEquals(4, searchResponse.getBody().length);
		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/25-1-2018"), GameDescription[].class);
		assertEquals(0, searchResponse.getBody().length);
		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/32-1-2018"), GameDescription[].class);
		assertEquals(0, searchResponse.getBody().length);

		// View

        ResponseEntity<Game> viewResponse = restTemplate.getForEntity(urlOf("/api/view/game/1516200793797"), Game.class);
        assertEquals("BRAZIL", viewResponse.getBody().gethTeam().getName());

		// Delete

		gameService.deleteGame(1516200793797L, UserId.VBR_USER_ID);
		gameService.deleteGame(1516200802314L, UserId.VBR_USER_ID);
		gameService.deleteGame(1516200804146L, UserId.VBR_USER_ID);
		gameService.deleteGame(1516200806997L, UserId.VBR_USER_ID);
	}

	@Test
	public void testDeleteLiveGame() {
		Game liveGame = createTestGame(12345L, GameStatus.LIVE);
		gameService.createGame(liveGame);

		GameStatistics gameStatistics = gameService.getGameStatistics();
		assertEquals(1, gameStatistics.getGamesCount());
		assertEquals(1, gameStatistics.getLiveGamesCount());

		gameService.deleteLiveGame(liveGame.getDate());

		gameStatistics = gameService.getGameStatistics();
		assertEquals(0, gameStatistics.getGamesCount());
		assertEquals(0, gameStatistics.getLiveGamesCount());
	}

	@Test
	public void testDeleteOldLiveGames() {
		Game liveGame = createTestGame(12345L, GameStatus.LIVE);
		Game game = createTestGame(123456L, GameStatus.COMPLETED);

		gameService.createGame(liveGame);
		gameService.createGame(game);

		GameStatistics gameStatistics = gameService.getGameStatistics();
		assertEquals(2, gameStatistics.getGamesCount());
		assertEquals(1, gameStatistics.getLiveGamesCount());

		gameService.deleteOldLiveGames(4);

		gameStatistics = gameService.getGameStatistics();
		assertEquals(1, gameStatistics.getGamesCount());
		assertEquals(0, gameStatistics.getLiveGamesCount());

		gameService.deleteGame(game.getDate(), UserId.VBR_USER_ID);
	}

	@Test
	public void testDeleteOldGames() {
		Game liveGame = createTestGame(12345L, GameStatus.LIVE);
		Game game = createTestGame(123456L, GameStatus.COMPLETED);

		gameService.createGame(liveGame);
		gameService.createGame(game);

		GameStatistics gameStatistics = gameService.getGameStatistics();
		assertEquals(2, gameStatistics.getGamesCount());
		assertEquals(1, gameStatistics.getLiveGamesCount());

		gameService.deleteOldGames(300);

		gameStatistics = gameService.getGameStatistics();
		assertEquals(0, gameStatistics.getGamesCount());
		assertEquals(0, gameStatistics.getLiveGamesCount());
	}

	@Test
	public void testDeleteTestGames() {
		// Add

		ResponseEntity<String> createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200793797"), HttpMethod.PUT, entityOf(game1), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200802314"), HttpMethod.PUT, entityOf(game2), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200804146"), HttpMethod.PUT, entityOf(game3), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		createResponse = restTemplate.exchange(urlOf("/api/manage/game/1516200806997"), HttpMethod.PUT, entityOf(game4), String.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		// Delete

		gameService.deleteTestGames(5);
		GameStatistics gameStatistics = gameService.getGameStatistics();
		assertEquals(3, gameStatistics.getGamesCount());

		gameService.deleteTestGames(15);
		gameStatistics = gameService.getGameStatistics();
		assertEquals(2, gameStatistics.getGamesCount());

		gameService.deleteOldLiveGames(1);
        gameStatistics = gameService.getGameStatistics();
		assertEquals(0, gameStatistics.getGamesCount());
	}

	@Test
	public void testMessages() {
		ResponseEntity<Boolean> hasMessageResponse = restTemplate.getForEntity(urlOf("/api/message/has"), Boolean.class);
		assertEquals(false, hasMessageResponse.getBody());

		String expected = "This is a test message";
		messageService.addMessage(expected);

		hasMessageResponse = restTemplate.getForEntity(urlOf("/api/message/has"), Boolean.class);
		assertEquals(true, hasMessageResponse.getBody());

		ResponseEntity<String> messageResponse = restTemplate.getForEntity(urlOf("/api/message"), String.class);
		assertEquals(expected, messageResponse.getBody());

		messageService.removeMessage();

		hasMessageResponse = restTemplate.getForEntity(urlOf("/api/message/has"), Boolean.class);
		assertEquals(false, hasMessageResponse.getBody());

		messageResponse = restTemplate.getForEntity(urlOf("/api/message"), String.class);
		assertEquals(null, messageResponse.getBody());
	}

	private String urlOf(String apiUrl) {
		return "http://localhost:" + port + apiUrl;
	}

	private HttpHeaders jsonHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private HttpEntity<String> entityOf(String json) {
		return new HttpEntity<>(json, jsonHeader());
	}

	private String game1 = "{\"kind\":\"INDOOR\",\"date\":1516200793797,\"schedule\":1516200793797,\"gender\":\"GENTS\",\"usage\":\"NORMAL\",\"status\":\"COMPLETED\",\"referee\":\"VBR\",\"league\":\"FIVB Volleyball World League 2017\",\"division\":\"\",\"userId\":\"01022018@vbr\",\"hTeam\":{\"name\":\"BRAZIL\",\"userId\":\"01022018@vbr\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#f3bc07\",\"liberoColor\":\"#034694\",\"players\":[1,3,4,5,9,10,11,13,16,18,19,20],\"liberos\":[6,8],\"captain\":1,\"gender\":\"GENTS\"},\"gTeam\":{\"name\":\"FRANCE\",\"userId\":\"01022018@vbr\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#034694\",\"liberoColor\":\"#bc0019\",\"players\":[5,6,8,9,10,11,12,14,16,17,18,21],\"liberos\":[2,20],\"captain\":6,\"gender\":\"GENTS\"},\"hSets\":2,\"gSets\":3,\"sets\":[{\"duration\":734,\"hPoints\":25,\"gPoints\":21,\"hTimeouts\":2,\"gTimeouts\":1,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":3,\"pos\":1},{\"num\":4,\"pos\":5},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":6},{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4}],\"hStartingPlayers\":[{\"num\":1,\"pos\":3},{\"num\":4,\"pos\":6},{\"num\":13,\"pos\":2},{\"num\":16,\"pos\":5},{\"num\":18,\"pos\":1},{\"num\":19,\"pos\":4}],\"gStartingPlayers\":[{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4},{\"num\":21,\"pos\":6}],\"hSubstitutions\":[{\"pIn\":3,\"pOut\":13,\"hPoints\":17,\"gPoints\":14}],\"gSubstitutions\":[],\"hCaptain\":1,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[{\"hPoints\":17,\"gPoints\":14}],\"rTime\":0},{\"duration\":758,\"hPoints\":15,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":8,\"pos\":5},{\"num\":9,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1},{\"num\":20,\"pos\":6}],\"gCurrentPlayers\":[{\"num\":5,\"pos\":6},{\"num\":6,\"pos\":2},{\"num\":9,\"pos\":3},{\"num\":10,\"pos\":4},{\"num\":12,\"pos\":5},{\"num\":21,\"pos\":1}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":13,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":20,\"pOut\":1,\"hPoints\":12,\"gPoints\":19},{\"pIn\":9,\"pOut\":4,\"hPoints\":12,\"gPoints\":19}],\"gSubstitutions\":[],\"hCaptain\":20,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":1262,\"hPoints\":23,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\"],\"serving\":\"G\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":8,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gCurrentPlayers\":[{\"num\":8,\"pos\":3},{\"num\":9,\"pos\":6},{\"num\":10,\"pos\":1},{\"num\":11,\"pos\":5},{\"num\":12,\"pos\":2},{\"num\":21,\"pos\":4}],\"hStartingPlayers\":[{\"num\":1,\"pos\":4},{\"num\":4,\"pos\":1},{\"num\":13,\"pos\":3},{\"num\":16,\"pos\":6},{\"num\":18,\"pos\":2},{\"num\":19,\"pos\":5}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":3,\"pOut\":13,\"hPoints\":6,\"gPoints\":11}],\"gSubstitutions\":[{\"pIn\":11,\"pOut\":6,\"hPoints\":17,\"gPoints\":18},{\"pIn\":8,\"pOut\":5,\"hPoints\":17,\"gPoints\":18}],\"hCaptain\":1,\"gCaptain\":8,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":1384,\"hPoints\":25,\"gPoints\":19,\"hTimeouts\":1,\"gTimeouts\":1,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":3,\"pos\":1},{\"num\":9,\"pos\":5},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3},{\"num\":20,\"pos\":2}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":5},{\"num\":5,\"pos\":4},{\"num\":9,\"pos\":1},{\"num\":10,\"pos\":2},{\"num\":11,\"pos\":6},{\"num\":12,\"pos\":3}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":3,\"pos\":5},{\"num\":4,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":2},{\"num\":6,\"pos\":4},{\"num\":9,\"pos\":5},{\"num\":10,\"pos\":6},{\"num\":12,\"pos\":1},{\"num\":21,\"pos\":3}],\"hSubstitutions\":[{\"pIn\":9,\"pOut\":4,\"hPoints\":20,\"gPoints\":15},{\"pIn\":20,\"pOut\":1,\"hPoints\":20,\"gPoints\":15}],\"gSubstitutions\":[{\"pIn\":8,\"pOut\":5,\"hPoints\":7,\"gPoints\":3},{\"pIn\":11,\"pOut\":6,\"hPoints\":20,\"gPoints\":15},{\"pIn\":5,\"pOut\":8,\"hPoints\":20,\"gPoints\":15}],\"hCaptain\":9,\"gCaptain\":9,\"hCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":7}],\"gCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":3}],\"rTime\":0},{\"duration\":1084,\"hPoints\":13,\"gPoints\":15,\"hTimeouts\":0,\"gTimeouts\":1,\"ladder\":[\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":4,\"pos\":5},{\"num\":13,\"pos\":1},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":5},{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":3,\"pos\":5},{\"num\":4,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4},{\"num\":21,\"pos\":6}],\"hSubstitutions\":[{\"pIn\":13,\"pOut\":3,\"hPoints\":11,\"gPoints\":10}],\"gSubstitutions\":[],\"hCaptain\":1,\"gCaptain\":6,\"hCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":7},{\"hPoints\":11,\"gPoints\":11}],\"gCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":8}],\"rTime\":0}],\"hCards\":[],\"gCards\":[],\"rules\":{\"userId\":\"01022018@vbr\",\"name\":\"Test Rules\",\"date\":1523199473000,\"setsPerGame\":5,\"pointsPerSet\":25,\"tieBreakInLastSet\":true,\"pointsInTieBreak\":15,\"twoPointsDifference\":true,\"sanctions\":true,\"teamTimeouts\":true,\"teamTimeoutsPerSet\":2,\"teamTimeoutDuration\":30,\"technicalTimeouts\":true,\"technicalTimeoutDuration\":60,\"gameIntervals\":true,\"gameIntervalDuration\":180,\"teamSubstitutionsPerSet\":6,\"beachCourtSwitches\":false,\"beachCourtSwitchFreq\":0,\"beachCourtSwitchFreqTieBreak\":0,\"customConsecutiveServesPerPlayer\": 9999}}";
	private String game2 = "{\"kind\":\"BEACH\",\"date\":1516200802314,\"schedule\":1516200802314,\"gender\":\"GENTS\",\"usage\":\"NORMAL\",\"status\":\"LIVE\",\"referee\":\"VBR\",\"league\":\"FIVB Beach Volleyball World Championship 2017\",\"division\":\"\",\"userId\":\"01022018@vbr\",\"hTeam\":{\"name\":\"USA\",\"userId\":\"01022018@vbr\",\"kind\":\"BEACH\",\"date\":1523199473000,\"color\":\"#bc0019\",\"liberoColor\":\"#ffffff\",\"players\":[1,2],\"liberos\":[],\"captain\":-1,\"gender\":\"GENTS\"},\"gTeam\":{\"name\":\"ITALY\",\"userId\":\"01022018@vbr\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#2980b9\",\"liberoColor\":\"#ffffff\",\"players\":[1,2],\"liberos\":[],\"captain\":-1,\"gender\":\"GENTS\"},\"hSets\":2,\"gSets\":0,\"sets\":[{\"duration\":317,\"hPoints\":21,\"gPoints\":18,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":1},{\"num\":2,\"pos\":2}],\"gCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[{\"hPoints\":16,\"gPoints\":16}],\"gCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":8}],\"rTime\":0},{\"duration\":373,\"hPoints\":21,\"gPoints\":17,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"gCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[{\"hPoints\":14,\"gPoints\":15}],\"gCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":6}],\"rTime\":0}],\"hCards\":[],\"gCards\":[],\"rules\":{\"userId\":\"01022018@vbr\",\"name\":\"Test Rules\",\"date\":1523199473000,\"setsPerGame\":5,\"pointsPerSet\":25,\"tieBreakInLastSet\":true,\"pointsInTieBreak\":15,\"twoPointsDifference\":true,\"sanctions\":true,\"teamTimeouts\":true,\"teamTimeoutsPerSet\":2,\"teamTimeoutDuration\":30,\"technicalTimeouts\":true,\"technicalTimeoutDuration\":60,\"gameIntervals\":true,\"gameIntervalDuration\":180,\"teamSubstitutionsPerSet\":6,\"beachCourtSwitches\":false,\"beachCourtSwitchFreq\":0,\"beachCourtSwitchFreqTieBreak\":0,\"customConsecutiveServesPerPlayer\": 9999}}";
	private String game3 = "{\"kind\":\"INDOOR\",\"date\":1516200804146,\"schedule\":1516200804146,\"gender\":\"LADIES\",\"usage\":\"POINTS_SCOREBOARD\",\"status\":\"COMPLETED\",\"referee\":\"VBR\",\"league\":\"\",\"division\":\"\",\"userId\":\"01022018@vbr\",\"hTeam\":{\"name\":\"Team A\",\"userId\":\"01022018@vbr\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#052443\",\"liberoColor\":\"#000000\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"LADIES\"},\"gTeam\":{\"name\":\"Team B\",\"userId\":\"01022018@vbr\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#e25618\",\"liberoColor\":\"#000000\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"LADIES\"},\"hSets\":3,\"gSets\":0,\"sets\":[{\"duration\":600000,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":600000,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":600000,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"firstServing\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0}],\"hCards\":[],\"gCards\":[],\"rules\":{\"userId\":\"01022018@vbr\",\"name\":\"Test Rules\",\"date\":1523199473000,\"setsPerGame\":5,\"pointsPerSet\":25,\"tieBreakInLastSet\":true,\"pointsInTieBreak\":15,\"twoPointsDifference\":true,\"sanctions\":true,\"teamTimeouts\":true,\"teamTimeoutsPerSet\":2,\"teamTimeoutDuration\":30,\"technicalTimeouts\":true,\"technicalTimeoutDuration\":60,\"gameIntervals\":true,\"gameIntervalDuration\":180,\"teamSubstitutionsPerSet\":6,\"beachCourtSwitches\":false,\"beachCourtSwitchFreq\":0,\"beachCourtSwitchFreqTieBreak\":0,\"customConsecutiveServesPerPlayer\": 9999}}";
	private String game4 = "{\"kind\":\"TIME\",\"date\":1516200806997,\"schedule\":1516200806997,\"gender\":\"MIXED\",\"usage\":\"TIME_SCOREBOARD\",\"status\":\"LIVE\",\"referee\":\"VBR\",\"league\":\"Tournament X\",\"division\":\"Pool A\",\"userId\":\"01022018@vbr\",\"hTeam\":{\"name\":\"Team 1\",\"userId\":\"01022018@vbr\",\"kind\":\"TIME\",\"date\":1523199473000,\"color\":\"#006032\",\"liberoColor\":\"#ffffff\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"MIXED\"},\"gTeam\":{\"name\":\"Team 2\",\"userId\":\"01022018@vbr\",\"kind\":\"TIME\",\"date\":1523199473000,\"color\":\"#ffffff\",\"liberoColor\":\"#ffffff\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"MIXED\"},\"hSets\":0,\"gSets\":0,\"sets\":[{\"duration\":247,\"hPoints\":49,\"gPoints\":49,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"firstServing\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":254000}],\"hCards\":[],\"gCards\":[],\"rules\":{\"userId\":\"01022018@vbr\",\"name\":\"Test Rules\",\"date\":1523199473000,\"setsPerGame\":5,\"pointsPerSet\":25,\"tieBreakInLastSet\":true,\"pointsInTieBreak\":15,\"twoPointsDifference\":true,\"sanctions\":true,\"teamTimeouts\":true,\"teamTimeoutsPerSet\":2,\"teamTimeoutDuration\":30,\"technicalTimeouts\":true,\"technicalTimeoutDuration\":60,\"gameIntervals\":true,\"gameIntervalDuration\":180,\"teamSubstitutionsPerSet\":6,\"beachCourtSwitches\":false,\"beachCourtSwitchFreq\":0,\"beachCourtSwitchFreqTieBreak\":0,\"customConsecutiveServesPerPlayer\": 9999}}";
	private String set2Game1 = "{\"duration\":758,\"hPoints\":15,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"firstServing\":\"H\",\"hCurrentPlayers\":[{\"num\":8,\"pos\":5},{\"num\":9,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1},{\"num\":20,\"pos\":6}],\"gCurrentPlayers\":[{\"num\":5,\"pos\":6},{\"num\":6,\"pos\":2},{\"num\":9,\"pos\":3},{\"num\":10,\"pos\":4},{\"num\":12,\"pos\":5},{\"num\":21,\"pos\":1}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":13,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":20,\"pOut\":1,\"hPoints\":12,\"gPoints\":19},{\"pIn\":9,\"pOut\":4,\"hPoints\":12,\"gPoints\":19}],\"gSubstitutions\":[],\"hCaptain\":20,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0}";

	private Game createTestGame(long date, GameStatus status) {
		Game game = new Game();
        game.setUserId(UserId.VBR_USER_ID);
		game.setKind("kind");
		game.setDate(date);
		game.setGender("gender");
		game.setUsage("usage");
		game.setStatus(status.toString());
		game.setLeague("league");
		game.sethTeam(new Team());
		game.gethTeam().setName("team");
		game.setgTeam(new Team());
		game.getgTeam().setName("team");
		game.sethSets(0);
		game.setgSets(0);
		game.setRules(new Rules());
		game.getRules().setName("rules");
		return game;
	}

	private String testUser = "01022018@google";

	private String rules = "{\"userId\":\"01022018@google\",\"name\":\"Test Rules\",\"date\":1523199473000,\"setsPerGame\":5,\"pointsPerSet\":25,\"tieBreakInLastSet\":true,\"pointsInTieBreak\":15,\"twoPointsDifference\":true,\"sanctions\":true,\"teamTimeouts\":true,\"teamTimeoutsPerSet\":2,\"teamTimeoutDuration\":30,\"technicalTimeouts\":true,\"technicalTimeoutDuration\":60,\"gameIntervals\":true,\"gameIntervalDuration\":180,\"teamSubstitutionsPerSet\":6,\"beachCourtSwitches\":false,\"beachCourtSwitchFreq\":0,\"beachCourtSwitchFreqTieBreak\":0,\"customConsecutiveServesPerPlayer\": 9999}";

	@Test
	public void testUserRules() {
        ResponseEntity<Rules> updateResponse = restTemplate.exchange(urlOf("/api/user/rules"), HttpMethod.PUT, entityOf(rules), Rules.class);
        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());

        ResponseEntity<Rules> createResponse = restTemplate.exchange(urlOf("/api/user/rules"), HttpMethod.POST, entityOf(rules), Rules.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals("Test Rules", createResponse.getBody().getName());

        createResponse = restTemplate.exchange(urlOf("/api/user/rules"), HttpMethod.POST, entityOf(rules), Rules.class);
        assertEquals(HttpStatus.CONFLICT, createResponse.getStatusCode());

        updateResponse = restTemplate.exchange(urlOf("/api/user/rules"), HttpMethod.PUT, entityOf(rules), Rules.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
		assertEquals("Test Rules", updateResponse.getBody().getName());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
                .queryParam("userId", testUser);
        ResponseEntity<Rules[]> rulesListResponse = restTemplate.getForEntity(builder.toUriString(), Rules[].class);
        assertEquals(1, rulesListResponse.getBody().length);
        assertEquals("Test Rules", rulesListResponse.getBody()[0].getName());

        rulesListResponse = restTemplate.getForEntity(urlOf("/api/user/rules/default"), Rules[].class);
        assertEquals(3, rulesListResponse.getBody().length);

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
                .queryParam("name", "Test Rules");
        ResponseEntity<Rules> rulesResponse = restTemplate.getForEntity(builder.toUriString(), Rules.class);
        assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
                .queryParam("name", "Unknown Rules");
        rulesResponse = restTemplate.getForEntity(builder.toUriString(), Rules.class);
        assertEquals(HttpStatus.NOT_FOUND, rulesResponse.getStatusCode());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules/count"))
				.queryParam("userId", testUser);
		ResponseEntity<Long> rulesCountResponse = restTemplate.getForEntity(builder.toUriString(), Long.class);
		assertEquals(1L, rulesCountResponse.getBody().longValue());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
                .queryParam("userId", testUser)
                .queryParam("name", "Test Rules");;
        restTemplate.delete(builder.toUriString());
	}

	private String team1 = "{\"userId\":\"01022018@google\",\"name\":\"BRAZIL\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#f3bc07\",\"liberoColor\":\"#034694\",\"players\":[1,3,4,5,9,10,11,13,16,18,19,20],\"liberos\":[6,8],\"captain\":1,\"gender\":\"GENTS\"}";
	private String team2 = "{\"userId\":\"01022018@google\",\"name\":\"FRANCE\",\"kind\":\"INDOOR\",\"date\":1523199473000,\"color\":\"#034694\",\"liberoColor\":\"#bc0019\",\"players\":[5,6,8,9,10,11,12,14,16,17,18,21],\"liberos\":[2,20],\"captain\":6,\"gender\":\"GENTS\"}";

	@Test
    public void testUserTeams() {
        ResponseEntity<Team> updateResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.PUT, entityOf(team1), Team.class);
        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());

        ResponseEntity<Team> createResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.POST, entityOf(team1), Team.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals("BRAZIL", createResponse.getBody().getName());

        createResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.POST, entityOf(team2), Team.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals("FRANCE", createResponse.getBody().getName());

        createResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.POST, entityOf(team1), Team.class);
        assertEquals(HttpStatus.CONFLICT, createResponse.getStatusCode());

        updateResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.PUT, entityOf(team1), Team.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("BRAZIL", updateResponse.getBody().getName());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser);
        ResponseEntity<Team[]> teamListResponse = restTemplate.getForEntity(builder.toUriString(), Team[].class);
        assertEquals(2, teamListResponse.getBody().length);
        assertEquals("BRAZIL", teamListResponse.getBody()[0].getName());
        assertEquals("FRANCE", teamListResponse.getBody()[1].getName());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
				.queryParam("kind", "INDOOR");
		teamListResponse = restTemplate.getForEntity(builder.toUriString(), Team[].class);
		assertEquals(2, teamListResponse.getBody().length);
		assertEquals("BRAZIL", teamListResponse.getBody()[0].getName());
		assertEquals("FRANCE", teamListResponse.getBody()[1].getName());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
				.queryParam("kind", "BEACH");
		teamListResponse = restTemplate.getForEntity(builder.toUriString(), Team[].class);
		assertEquals(0, teamListResponse.getBody().length);

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
        ResponseEntity<Team> teamResponse = restTemplate.getForEntity(builder.toUriString(), Team.class);
        assertEquals(HttpStatus.OK, teamResponse.getStatusCode());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "ITALY")
                .queryParam("gender", "GENTS");
        teamResponse = restTemplate.getForEntity(builder.toUriString(), Team.class);
        assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team/count"))
				.queryParam("userId", testUser);
        ResponseEntity<Long> teamCountResponse = restTemplate.getForEntity(builder.toUriString(), Long.class);
        assertEquals(2L, teamCountResponse.getBody().longValue());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
        restTemplate.delete(builder.toUriString());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
                .queryParam("userId", testUser)
                .queryParam("name", "FRANCE")
                .queryParam("gender", "GENTS");
        restTemplate.delete(builder.toUriString());
    }

    private String description1 = "{\"userId\":\"01022018@google\",\"kind\":\"INDOOR\",\"date\":1520674661962,\"schedule\":0,\"gender\":\"GENTS\",\"usage\":\"NORMAL\",\"status\":\"SCHEDULED\",\"referee\":\"VBR\",\"league\":\"FIVB Volleyball World League 2017\",\"division\":\"\",\"hName\":\"BRAZIL\",\"gName\":\"FRANCE\",\"hSets\":0,\"gSets\":0,\"rules\":\"Test Rules\"}";

    @Test
    public void testUserGames() {
        ResponseEntity<Rules> createRulesResponse = restTemplate.exchange(urlOf("/api/user/rules"), HttpMethod.POST, entityOf(rules), Rules.class);
        assertEquals(HttpStatus.CREATED, createRulesResponse.getStatusCode());

        ResponseEntity<Team> createTeamResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.POST, entityOf(team1), Team.class);
        assertEquals(HttpStatus.CREATED, createTeamResponse.getStatusCode());

        createTeamResponse = restTemplate.exchange(urlOf("/api/user/team"), HttpMethod.POST, entityOf(team2), Team.class);
        assertEquals(HttpStatus.CREATED, createTeamResponse.getStatusCode());

        ResponseEntity<GameDescription> gameResponse = restTemplate.exchange(urlOf("/api/user/game"), HttpMethod.PUT, entityOf(description1), GameDescription.class);
        assertEquals(HttpStatus.NOT_FOUND, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/user/game"), HttpMethod.POST, entityOf(description1), GameDescription.class);
        assertEquals(HttpStatus.CREATED, gameResponse.getStatusCode());

        gameResponse = restTemplate.exchange(urlOf("/api/user/game"), HttpMethod.PUT, entityOf(description1), GameDescription.class);
        assertEquals(HttpStatus.OK, gameResponse.getStatusCode());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/game/count"))
				.queryParam("userId", testUser);
        ResponseEntity<Long> gameCountResponse = restTemplate.getForEntity(builder.toUriString(), Long.class);
        assertEquals(1L, gameCountResponse.getBody().longValue());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/game"))
				.queryParam("userId", testUser)
                .queryParam("id", "1520674661962");
		ResponseEntity<Game> fullGameResponse = restTemplate.getForEntity(builder.toUriString(), Game.class);
        assertEquals(1520674661962L, fullGameResponse.getBody().getDate());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/game/code"))
				.queryParam("userId", testUser)
                .queryParam("id", "1520674661962");
        ResponseEntity<Integer> gameCodeResponse = restTemplate.getForEntity(builder.toUriString(), Integer.class);

        int code = gameCodeResponse.getBody();

        ResponseEntity<Game> viewResponse = restTemplate.getForEntity(urlOf("/api/view/game/code/" + code), Game.class);
        assertEquals("BRAZIL", viewResponse.getBody().gethTeam().getName());
        assertEquals("FRANCE", viewResponse.getBody().getgTeam().getName());

        // Will fail until they are not used

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
                .queryParam("name", "Test Rules");
        restTemplate.delete(builder.toUriString());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
				.queryParam("name", "Test Rules");
		ResponseEntity<Rules> rulesResponse = restTemplate.getForEntity(builder.toUriString(), Rules.class);
		assertEquals(HttpStatus.OK, rulesResponse.getStatusCode());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
        restTemplate.delete(builder.toUriString());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
				.queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
		ResponseEntity<Team> teamResponse = restTemplate.getForEntity(builder.toUriString(), Team.class);
		assertEquals(HttpStatus.OK, teamResponse.getStatusCode());

		// Delete game

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/game"))
                .queryParam("userId", testUser)
                .queryParam("id", "1520674661962");;
        restTemplate.delete(builder.toUriString());

        // Now the rules and teams are free

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
                .queryParam("name", "Test Rules");;
        restTemplate.delete(builder.toUriString());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/rules"))
				.queryParam("userId", testUser)
				.queryParam("name", "Test Rules");
		rulesResponse = restTemplate.getForEntity(builder.toUriString(), Rules.class);
		assertEquals(HttpStatus.NOT_FOUND, rulesResponse.getStatusCode());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
        restTemplate.delete(builder.toUriString());

        builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
                .queryParam("name", "FRANCE")
                .queryParam("gender", "GENTS");
        restTemplate.delete(builder.toUriString());

		builder = UriComponentsBuilder.fromHttpUrl(urlOf("/api/user/team"))
				.queryParam("userId", testUser)
				.queryParam("name", "BRAZIL")
                .queryParam("gender", "GENTS");
		teamResponse = restTemplate.getForEntity(builder.toUriString(), Team.class);
		assertEquals(HttpStatus.NOT_FOUND, teamResponse.getStatusCode());
    }
}
