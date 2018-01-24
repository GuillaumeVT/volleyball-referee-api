package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.model.Game;
import com.tonkar.volleyballreferee.model.GameDescription;
import com.tonkar.volleyballreferee.model.GameStatistics;
import com.tonkar.volleyballreferee.model.Team;
import com.tonkar.volleyballreferee.service.GameService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@ActiveProfiles("test")
public class VolleyballRefereeApplicationTests {

	@Autowired
	private GameService gameService;

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


		// Exist

		ResponseEntity<Boolean> existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/123"), Boolean.class);
		assertEquals(false, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200793797"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200802314"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200804146"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		existResponse = restTemplate.getForEntity(urlOf("/api/manage/game/1516200806997"), Boolean.class);
		assertEquals(true, existResponse.getBody());

		// Stats

		ResponseEntity<GameStatistics> statResponse = restTemplate.getForEntity(urlOf("/api/stats/game"), GameStatistics.class);
		assertEquals(4, statResponse.getBody().getGamesCount());
		assertEquals(2, statResponse.getBody().getLiveGamesCount());

		// Search

		ResponseEntity<GameDescription[]> searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/dummy"), GameDescription[].class);
		assertEquals(HttpStatus.NOT_FOUND, searchResponse.getStatusCode());

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

		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/17-1-2018"), GameDescription[].class);
		assertEquals(4, searchResponse.getBody().length);
		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/25-1-2018"), GameDescription[].class);
		assertEquals(0, searchResponse.getBody().length);
		searchResponse = restTemplate.getForEntity(urlOf("/api/search/game/date/32-1-2018"), GameDescription[].class);
		assertEquals(0, searchResponse.getBody().length);

		// Delete

		gameService.deleteGame(1516200793797L);
		gameService.deleteGame(1516200802314L);
		gameService.deleteGame(1516200804146L);
		gameService.deleteGame(1516200806997L);
	}

	@Test
	public void testDeleteLiveGame() {
		Game liveGame = createTestGame(12345L, true);
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
		Game liveGame = createTestGame(12345L, true);
		Game game = createTestGame(123456L, false);

		gameService.createGame(liveGame);
		gameService.createGame(game);

		GameStatistics gameStatistics = gameService.getGameStatistics();
		assertEquals(2, gameStatistics.getGamesCount());
		assertEquals(1, gameStatistics.getLiveGamesCount());

		gameService.deleteOldLiveGames(4);

		gameStatistics = gameService.getGameStatistics();
		assertEquals(1, gameStatistics.getGamesCount());
		assertEquals(0, gameStatistics.getLiveGamesCount());

		gameService.deleteGame(game.getDate());
	}

	@Test
	public void testDeleteOldGames() {
		Game liveGame = createTestGame(12345L, true);
		Game game = createTestGame(123456L, false);

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

	private String game1 = "{\"kind\":\"INDOOR\",\"date\":1516200793797,\"gender\":\"GENTS\",\"usage\":\"NORMAL\",\"live\":false,\"league\":\"FIVB Volleyball World League 2017\",\"hTeam\":{\"name\":\"BRAZIL\",\"color\":\"#f3bc07\",\"liberoColor\":\"#034694\",\"players\":[1,3,4,5,9,10,11,13,16,18,19,20],\"liberos\":[6,8],\"captain\":1,\"gender\":\"GENTS\"},\"gTeam\":{\"name\":\"FRANCE\",\"color\":\"#034694\",\"liberoColor\":\"#bc0019\",\"players\":[5,6,8,9,10,11,12,14,16,17,18,21],\"liberos\":[2,20],\"captain\":6,\"gender\":\"GENTS\"},\"hSets\":2,\"gSets\":3,\"sets\":[{\"duration\":734,\"hPoints\":25,\"gPoints\":21,\"hTimeouts\":2,\"gTimeouts\":1,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":3,\"pos\":1},{\"num\":4,\"pos\":5},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":6},{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4}],\"hStartingPlayers\":[{\"num\":1,\"pos\":3},{\"num\":4,\"pos\":6},{\"num\":13,\"pos\":2},{\"num\":16,\"pos\":5},{\"num\":18,\"pos\":1},{\"num\":19,\"pos\":4}],\"gStartingPlayers\":[{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4},{\"num\":21,\"pos\":6}],\"hSubstitutions\":[{\"pIn\":3,\"pOut\":13,\"hPoints\":17,\"gPoints\":14}],\"gSubstitutions\":[],\"hCaptain\":1,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[{\"hPoints\":17,\"gPoints\":14}],\"rTime\":0},{\"duration\":758,\"hPoints\":15,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"hCurrentPlayers\":[{\"num\":8,\"pos\":5},{\"num\":9,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1},{\"num\":20,\"pos\":6}],\"gCurrentPlayers\":[{\"num\":5,\"pos\":6},{\"num\":6,\"pos\":2},{\"num\":9,\"pos\":3},{\"num\":10,\"pos\":4},{\"num\":12,\"pos\":5},{\"num\":21,\"pos\":1}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":13,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":20,\"pOut\":1,\"hPoints\":12,\"gPoints\":19},{\"pIn\":9,\"pOut\":4,\"hPoints\":12,\"gPoints\":19}],\"gSubstitutions\":[],\"hCaptain\":20,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":1262,\"hPoints\":23,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\"],\"serving\":\"G\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":8,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gCurrentPlayers\":[{\"num\":8,\"pos\":3},{\"num\":9,\"pos\":6},{\"num\":10,\"pos\":1},{\"num\":11,\"pos\":5},{\"num\":12,\"pos\":2},{\"num\":21,\"pos\":4}],\"hStartingPlayers\":[{\"num\":1,\"pos\":4},{\"num\":4,\"pos\":1},{\"num\":13,\"pos\":3},{\"num\":16,\"pos\":6},{\"num\":18,\"pos\":2},{\"num\":19,\"pos\":5}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":3,\"pOut\":13,\"hPoints\":6,\"gPoints\":11}],\"gSubstitutions\":[{\"pIn\":11,\"pOut\":6,\"hPoints\":17,\"gPoints\":18},{\"pIn\":8,\"pOut\":5,\"hPoints\":17,\"gPoints\":18}],\"hCaptain\":1,\"gCaptain\":8,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":1384,\"hPoints\":25,\"gPoints\":19,\"hTimeouts\":1,\"gTimeouts\":1,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[{\"num\":3,\"pos\":1},{\"num\":9,\"pos\":5},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3},{\"num\":20,\"pos\":2}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":5},{\"num\":5,\"pos\":4},{\"num\":9,\"pos\":1},{\"num\":10,\"pos\":2},{\"num\":11,\"pos\":6},{\"num\":12,\"pos\":3}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":3,\"pos\":5},{\"num\":4,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":2},{\"num\":6,\"pos\":4},{\"num\":9,\"pos\":5},{\"num\":10,\"pos\":6},{\"num\":12,\"pos\":1},{\"num\":21,\"pos\":3}],\"hSubstitutions\":[{\"pIn\":9,\"pOut\":4,\"hPoints\":20,\"gPoints\":15},{\"pIn\":20,\"pOut\":1,\"hPoints\":20,\"gPoints\":15}],\"gSubstitutions\":[{\"pIn\":8,\"pOut\":5,\"hPoints\":7,\"gPoints\":3},{\"pIn\":11,\"pOut\":6,\"hPoints\":20,\"gPoints\":15},{\"pIn\":5,\"pOut\":8,\"hPoints\":20,\"gPoints\":15}],\"hCaptain\":9,\"gCaptain\":9,\"hCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":7}],\"gCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":3}],\"rTime\":0},{\"duration\":1084,\"hPoints\":13,\"gPoints\":15,\"hTimeouts\":0,\"gTimeouts\":1,\"ladder\":[\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":4,\"pos\":5},{\"num\":13,\"pos\":1},{\"num\":16,\"pos\":4},{\"num\":18,\"pos\":6},{\"num\":19,\"pos\":3}],\"gCurrentPlayers\":[{\"num\":2,\"pos\":5},{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":3,\"pos\":5},{\"num\":4,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":5},{\"num\":6,\"pos\":1},{\"num\":9,\"pos\":2},{\"num\":10,\"pos\":3},{\"num\":12,\"pos\":4},{\"num\":21,\"pos\":6}],\"hSubstitutions\":[{\"pIn\":13,\"pOut\":3,\"hPoints\":11,\"gPoints\":10}],\"gSubstitutions\":[],\"hCaptain\":1,\"gCaptain\":6,\"hCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":7},{\"hPoints\":11,\"gPoints\":11}],\"gCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":8}],\"rTime\":0}]}";
	private String game2 = "{\"kind\":\"BEACH\",\"date\":1516200802314,\"gender\":\"GENTS\",\"usage\":\"NORMAL\",\"live\":true,\"league\":\"FIVB Beach Volleyball World Championship 2017\",\"hTeam\":{\"name\":\"USA\",\"color\":\"#bc0019\",\"liberoColor\":\"#ffffff\",\"players\":[1,2],\"liberos\":[],\"captain\":-1,\"gender\":\"GENTS\"},\"gTeam\":{\"name\":\"ITALY\",\"color\":\"#2980b9\",\"liberoColor\":\"#ffffff\",\"players\":[1,2],\"liberos\":[],\"captain\":-1,\"gender\":\"GENTS\"},\"hSets\":2,\"gSets\":0,\"sets\":[{\"duration\":317,\"hPoints\":21,\"gPoints\":18,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":1},{\"num\":2,\"pos\":2}],\"gCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[{\"hPoints\":16,\"gPoints\":16}],\"gCalledTimeouts\":[{\"hPoints\":10,\"gPoints\":8}],\"rTime\":0},{\"duration\":373,\"hPoints\":21,\"gPoints\":17,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"H\",\"H\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"G\",\"H\",\"H\",\"G\",\"G\",\"H\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"gCurrentPlayers\":[{\"num\":1,\"pos\":2},{\"num\":2,\"pos\":1}],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[{\"hPoints\":14,\"gPoints\":15}],\"gCalledTimeouts\":[{\"hPoints\":7,\"gPoints\":6}],\"rTime\":0}]}";
	private String game3 = "{\"kind\":\"INDOOR\",\"date\":1516200804146,\"gender\":\"LADIES\",\"usage\":\"POINTS_SCOREBOARD\",\"live\":false,\"league\":\"\",\"hTeam\":{\"name\":\"Team A\",\"color\":\"#052443\",\"liberoColor\":\"#000000\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"LADIES\"},\"gTeam\":{\"name\":\"Team B\",\"color\":\"#e25618\",\"liberoColor\":\"#000000\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"LADIES\"},\"hSets\":3,\"gSets\":0,\"sets\":[{\"duration\":488,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":589,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0},{\"duration\":738,\"hPoints\":25,\"gPoints\":23,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"H\"],\"serving\":\"H\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":-1,\"gCaptain\":-1,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0}]}";
	private String game4 = "{\"kind\":\"INDOOR\",\"date\":1516200806997,\"gender\":\"MIXED\",\"usage\":\"TIME_SCOREBOARD\",\"live\":true,\"league\":\"Tournament X\",\"hTeam\":{\"name\":\"Team 1\",\"color\":\"#006032\",\"liberoColor\":\"#ffffff\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"MIXED\"},\"gTeam\":{\"name\":\"Team 2\",\"color\":\"#ffffff\",\"liberoColor\":\"#ffffff\",\"players\":[],\"liberos\":[],\"captain\":-1,\"gender\":\"MIXED\"},\"hSets\":0,\"gSets\":0,\"sets\":[{\"duration\":247,\"hPoints\":49,\"gPoints\":49,\"hTimeouts\":0,\"gTimeouts\":0,\"ladder\":[\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"hCurrentPlayers\":[],\"gCurrentPlayers\":[],\"hStartingPlayers\":[],\"gStartingPlayers\":[],\"hSubstitutions\":[],\"gSubstitutions\":[],\"hCaptain\":0,\"gCaptain\":0,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":254000}]}";
	private String set2Game1 = "{\"duration\":758,\"hPoints\":15,\"gPoints\":25,\"hTimeouts\":2,\"gTimeouts\":2,\"ladder\":[\"G\",\"H\",\"G\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"H\",\"G\",\"H\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"H\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"G\",\"G\",\"H\",\"G\",\"H\",\"G\"],\"serving\":\"G\",\"hCurrentPlayers\":[{\"num\":8,\"pos\":5},{\"num\":9,\"pos\":3},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1},{\"num\":20,\"pos\":6}],\"gCurrentPlayers\":[{\"num\":5,\"pos\":6},{\"num\":6,\"pos\":2},{\"num\":9,\"pos\":3},{\"num\":10,\"pos\":4},{\"num\":12,\"pos\":5},{\"num\":21,\"pos\":1}],\"hStartingPlayers\":[{\"num\":1,\"pos\":6},{\"num\":4,\"pos\":3},{\"num\":13,\"pos\":5},{\"num\":16,\"pos\":2},{\"num\":18,\"pos\":4},{\"num\":19,\"pos\":1}],\"gStartingPlayers\":[{\"num\":5,\"pos\":1},{\"num\":6,\"pos\":3},{\"num\":9,\"pos\":4},{\"num\":10,\"pos\":5},{\"num\":12,\"pos\":6},{\"num\":21,\"pos\":2}],\"hSubstitutions\":[{\"pIn\":20,\"pOut\":1,\"hPoints\":12,\"gPoints\":19},{\"pIn\":9,\"pOut\":4,\"hPoints\":12,\"gPoints\":19}],\"gSubstitutions\":[],\"hCaptain\":20,\"gCaptain\":6,\"hCalledTimeouts\":[],\"gCalledTimeouts\":[],\"rTime\":0}";

	private Game createTestGame(long date, boolean live) {
		Game game = new Game();
		game.setKind("kind");
		game.setDate(date);
		game.setGender("gender");
		game.setUsage("usage");
		game.setLive(live);
		game.setLeague("league");
		game.sethTeam(new Team());
		game.gethTeam().setName("team");
		game.setgTeam(new Team());
		game.getgTeam().setName("team");
		game.sethSets(0);
		game.setgSets(0);
		return game;
	}
}
