package com.credo.qa.tests;

import com.credo.qa.base.BaseTest;
import com.credo.qa.config.AppConfig;
import com.credo.qa.model.User;
import com.credo.qa.wiremock.WireMockSetup;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.common.mapper.TypeRef;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

public class UsersApiTest extends BaseTest {

    // ══════════════════════════════════════════════════════════════
    // POSITIVE TESTS
    // ══════════════════════════════════════════════════════════════

    @DataProvider(name = "getAllUsersData")
    public Object[][] getAllUsersData() {
        // {expectedStatusCode, expectedUserCount}
        return new Object[][] {
            {200, 2}
        };
    }

    @Test(dataProvider = "getAllUsersData",
          description = "GET /users აბრუნებს სწორ მომხმარებლებს სწორი ფორმატით")
    public void testGetAllUsers_Positive(int expectedStatus, int expectedCount) {
        List<User> users = given()
            .when()
                .get(AppConfig.USERS_ENDPOINT)
            .then()
                .statusCode(expectedStatus)
                .contentType("application/json")
                .extract()
                .as(new TypeRef<List<User>>() {});

        assertNotNull(users, "სია არ უნდა იყოს null");
        assertEquals(users.size(), expectedCount, "მომხმარებლების რაოდენობა არასწორია");

        // ვამოწმებთ პირველ მომხმარებელს (Alice)
        assertEquals(users.get(0).getId(), 1);
        assertEquals(users.get(0).getName(), "Alice");
        assertEquals(users.get(0).getAge(), 30);
        assertEquals(users.get(0).getGender(), "female");

        // ვამოწმებთ მეორე მომხმარებელს (Bob)
        assertEquals(users.get(1).getId(), 2);
        assertEquals(users.get(1).getName(), "Bob");
        assertEquals(users.get(1).getAge(), 25);
        assertEquals(users.get(1).getGender(), "male");
    }

    @DataProvider(name = "filterByAgeData")
    public Object[][] filterByAgeData() {
        // {age, expectedStatus, expectedCount, expectedName}
        return new Object[][] {
            {30, 200, 1, "Alice"},
            {25, 200, 1, "Bob"}
        };
    }

    @Test(dataProvider = "filterByAgeData",
          description = "GET /users?age=X აბრუნებს სწორ მომხმარებელს")
    public void testFilterByAge_Positive(int age, int expectedStatus, int expectedCount, String expectedName) {
        List<User> users = given()
            .queryParam("age", age)
            .when()
                .get(AppConfig.USERS_ENDPOINT)
            .then()
                .statusCode(expectedStatus)
                .extract()
                .as(new TypeRef<List<User>>() {});

        assertEquals(users.size(), expectedCount,
            "age=" + age + " ფილტრით უნდა დაბრუნდეს " + expectedCount + " მომხმარებელი");
        assertEquals(users.get(0).getName(), expectedName,
            "სახელი უნდა იყოს " + expectedName);
        assertEquals(users.get(0).getAge(), age,
            "age უნდა იყოს " + age);
    }

    @DataProvider(name = "filterByGenderData")
    public Object[][] filterByGenderData() {
        // {gender, expectedStatus, expectedCount}
        return new Object[][] {
            {"female", 200, 1},
            {"male",   200, 1}
        };
    }

    @Test(dataProvider = "filterByGenderData",
          description = "GET /users?gender=X აბრუნებს სწორ მომხმარებელს")
    public void testFilterByGender_Positive(String gender, int expectedStatus, int expectedCount) {
        List<User> users = given()
            .queryParam("gender", gender)
            .when()
                .get(AppConfig.USERS_ENDPOINT)
            .then()
                .statusCode(expectedStatus)
                .extract()
                .as(new TypeRef<List<User>>() {});

        assertEquals(users.size(), expectedCount,
            "gender=" + gender + " ფილტრით უნდა დაბრუნდეს " + expectedCount + " მომხმარებელი");
        assertEquals(users.get(0).getGender(), gender,
            "gender-ი უნდა იყოს " + gender);
    }

    // ══════════════════════════════════════════════════════════════
    // NEGATIVE TESTS
    // ══════════════════════════════════════════════════════════════

    @DataProvider(name = "invalidAgeData")
    public Object[][] invalidAgeData() {
        // {age, expectedStatus}
        return new Object[][] {
            {-1, 400}
        };
    }

    @Test(dataProvider = "invalidAgeData",
          description = "GET /users?age=-1 აბრუნებს 400 Bad Request")
    public void testInvalidAge_Negative(int age, int expectedStatus) {
        given()
            .queryParam("age", age)
            .when()
                .get(AppConfig.USERS_ENDPOINT)
            .then()
                .statusCode(expectedStatus);
    }

    @DataProvider(name = "serverErrorData")
    public Object[][] serverErrorData() {
        // {expectedStatus}
        return new Object[][] {
            {500}
        };
    }

    @Test(dataProvider = "serverErrorData",
          description = "GET /users აბრუნებს 500 Internal Server Error")
    public void testInternalServerError_Negative(int expectedStatus) throws Exception {
        // ვანახლებთ GET /users სტაბს 500 ერორზე ამ ტესტისთვის
        WireMock.reset();
        WireMock.stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse().withStatus(500)));

        try {
            given()
                .when()
                    .get(AppConfig.USERS_ENDPOINT)
                .then()
                    .statusCode(expectedStatus);
        } finally {
            // ტესტის შემდეგ ვაღდგენთ ყველა სტაბს
            WireMockSetup.configure();
        }
    }

    @DataProvider(name = "invalidGenderData")
    public Object[][] invalidGenderData() {
        // {gender, expectedStatus}
        return new Object[][] {
            {"unknown", 422}
        };
    }

    @Test(dataProvider = "invalidGenderData",
          description = "GET /users?gender=unknown აბრუნებს 422 Unprocessable Entity")
    public void testInvalidGender_Negative(String gender, int expectedStatus) {
        given()
            .queryParam("gender", gender)
            .when()
                .get(AppConfig.USERS_ENDPOINT)
            .then()
                .statusCode(expectedStatus);
    }
}
