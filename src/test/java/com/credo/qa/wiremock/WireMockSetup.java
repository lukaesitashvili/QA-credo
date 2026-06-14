package com.credo.qa.wiremock;

import com.credo.qa.config.AppConfig;
import com.credo.qa.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockSetup {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ზუსტი ტესტ მონაცემები დავალების მოთხოვნის მიხედვით
    public static final List<User> ALL_USERS = Arrays.asList(
        User.builder().id(1).name("Alice").age(30).gender("female").build(),
        User.builder().id(2).name("Bob").age(25).gender("male").build()
    );

    public static void configure() throws Exception {
        WireMock.configureFor(AppConfig.WIREMOCK_HOST, AppConfig.WIREMOCK_PORT);
        WireMock.reset();

        registerPositiveStubs();
        registerNegativeStubs();
    }

    private static void registerPositiveStubs() throws Exception {
        // GET /users → 200 ყველა მომხმარებელი
        stubFor(get(urlEqualTo("/users"))
            .willReturn(okJson(mapper.writeValueAsString(ALL_USERS))));

        // GET /users?age=30 → მხოლოდ Alice
        List<User> age30 = Arrays.asList(ALL_USERS.get(0));
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("age", equalTo("30"))
            .willReturn(okJson(mapper.writeValueAsString(age30))));

        // GET /users?age=25 → მხოლოდ Bob
        List<User> age25 = Arrays.asList(ALL_USERS.get(1));
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("age", equalTo("25"))
            .willReturn(okJson(mapper.writeValueAsString(age25))));

        // GET /users?gender=female → მხოლოდ Alice
        List<User> females = Arrays.asList(ALL_USERS.get(0));
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("gender", equalTo("female"))
            .willReturn(okJson(mapper.writeValueAsString(females))));

        // GET /users?gender=male → მხოლოდ Bob
        List<User> males = Arrays.asList(ALL_USERS.get(1));
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("gender", equalTo("male"))
            .willReturn(okJson(mapper.writeValueAsString(males))));
    }

    private static void registerNegativeStubs() {
        // GET /users?age=-1 → 400 Bad Request
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("age", equalTo("-1"))
            .willReturn(aResponse()
                .withStatus(400)
                .withBody("Bad Request: age must be a positive number")));

        // GET /users?gender=unknown → 422 Unprocessable Entity
        stubFor(get(urlPathEqualTo("/users"))
            .withQueryParam("gender", equalTo("unknown"))
            .willReturn(aResponse()
                .withStatus(422)
                .withBody("Unprocessable Entity: invalid gender value")));
    }
}
