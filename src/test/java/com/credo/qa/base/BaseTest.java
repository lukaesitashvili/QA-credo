package com.credo.qa.base;

import com.credo.qa.config.AppConfig;
import com.credo.qa.wiremock.WireMockSetup;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        RestAssured.baseURI = AppConfig.BASE_URL;
        WireMockSetup.configure();
    }
}
