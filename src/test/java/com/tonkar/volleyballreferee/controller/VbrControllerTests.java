package com.tonkar.volleyballreferee.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.security.SecurityConfiguration;
import com.tonkar.volleyballreferee.service.*;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@WebMvcTest
@Import({ SecurityConfiguration.class })
@ComponentScan(basePackages = "com.tonkar.volleyballreferee.security")
@ContextConfiguration(classes = ControllerExceptionHandler.class)
@ActiveProfiles("test")
class VbrControllerTests {

    @Autowired
    private MockMvc mockMvc;

    protected final Faker faker = new Faker(Locale.ENGLISH);

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Value("${vbr.jwt.key}")
    private String jwtKey;

    @Value("${vbr.jwt.issuer}")
    private String issuer;

    @Value("${vbr.jwt.token-validity-days}")
    private int tokenValidityDays;

    protected WebTestClient webTestClient;

    protected final String userToken    = "userToken";
    protected final String adminToken   = "adminToken";
    protected final String invalidToken = "invalidToken";

    private final UUID userId  = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    @PostConstruct
    public void init() {
        webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();

        Mockito.doReturn(generateJwtToken(userId)).when(authService).verifyToken(userToken);
        Mockito.doReturn(generateJwtToken(adminId)).when(authService).verifyToken(adminToken);
        Mockito.doThrow(BadCredentialsException.class).when(authService).verifyToken(invalidToken);

        Mockito.doReturn(generateUser(userId, false)).when(userService).getUser(userId);
        Mockito.doReturn(generateUser(adminId, true)).when(userService).getUser(adminId);
    }

    protected String bearer(String token) {
        return "Bearer %s".formatted(token);
    }

    private DecodedJWT generateJwtToken(UUID id) {
        ZonedDateTime iat = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L);
        ZonedDateTime exp = iat.plusDays(tokenValidityDays);

        String token = JWT
                .create()
                .withIssuer(issuer)
                .withSubject(id.toString())
                .withIssuedAt(iat.toInstant())
                .withExpiresAt(exp.toInstant())
                .sign(Algorithm.HMAC256(jwtKey.getBytes(StandardCharsets.UTF_8)));

        return JWT.decode(token);
    }

    private User generateUser(UUID id, boolean isAdmin) {
        User user = new User();
        user.setId(userId);
        user.setEnabled(true);
        user.setPseudo(faker.name().firstName());
        user.setAdmin(isAdmin);
        return user;
    }
}
