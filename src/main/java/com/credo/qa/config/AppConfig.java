package com.credo.qa.config;

public class AppConfig {

    public static final String WIREMOCK_HOST = "localhost";
    public static final int WIREMOCK_PORT = 8080;
    public static final String BASE_URL = "http://" + WIREMOCK_HOST + ":" + WIREMOCK_PORT;
    public static final String USERS_ENDPOINT = "/users";
    public static final String DB_URL = "jdbc:sqlite:test_results.db";
}
