package io.bankbridge.handler;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class BanksRemoteCallsTest {

    @Test
    // Test V1 endpoint without parameters
    // It should return status 200
    public void basicTestV1() {
        given().when().get("http://localhost:8080/v1/banks/all").then().statusCode(200);
    }

    @Test
    // Test V2 endpoint without parameters
    // It should return status 200
    public void basicTestV2() {
        given().when().get("http://localhost:8080/v2/banks/all").then().statusCode(200);
    }

    @Test
    // Test V1 endpoint without parameters
    // It should return result with default page size of 5
    public void basicTestV1NumResult() {
        given().when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(5));
    }

    @Test
    // Test V2 endpoint without parameters
    // It should return result with default page size of 5
    public void basicTestV2NumResult() {
        given().when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(5));
    }

    @Test
    // Test V1 endpoint with invalid countryCode parameters
    // It should return size 0
    public void TestInvalidV1CountryCode() {
        given().param("countryCode", "DO").when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(0));
    }

    @Test
    // Test V1 endpoint with invalid countryCode parameters
    // It should return size 0
    public void TestInvalidV2CountryCode() {
        given().param("countryCode", "ES").when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(0));
    }

    @Test
    // Test V1 endpoint with a valid countryCode parameters
    // It should return size 3, because that is the max record
    public void TestValidV1CountryCode() {
        given().param("countryCode", "DE").when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(3));
    }

    @Test
    // Test V1 endpoint with valid countryCode parameters
    // It should return size 5 because of default page size
    public void TestValidV2CountryCode() {
        given().param("countryCode", "NO").when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(5));
    }

    @Test
    // Test V1 endpoint with valid countryCode, offset and limit parameters
    // It should return size 2, because limit is 2
    public void TestValidV1CountryCodeLimitOffset() {
        given().param("countryCode", "CH").param("offset", 0).param("limit", 2).when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(2));
    }

    @Test
    // Test V1 endpoint with valid countryCode, limit and offset parameters
    // It should return size 6, becuase limit is greater than default page 5
    public void TestValidV2CountryCodeLimitOffset() {
        given().param("countryCode", "NO").param("offset", 0).param("limit", 20).when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(6));
    }

    @Test
    // Test V1 endpoint with valid countryCode parameters and out of range offset with limt
    // It should return size 0
    public void TestOutOfRangeV1Offset() {
        given().param("countryCode", "CH").param("offset", 20).param("limit", 2).when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(0));
    }

    @Test
    // Test V2 endpoint with valid countryCode parameters and out of range offset with limt
    // It should return size 0
    public void TestOutOfRangeV2Offset() {
        given().param("countryCode", "NO").param("offset", 20).param("limit", 20).when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(0));
    }

    @Test
    // Test V1 endpoint with no limit parameter
    // It should return 400 Status code
    public void TestV1NoLimit() {
        given().param("offset", 1).when().get("http://localhost:8080/v1/banks/all").then().statusCode(400);
    }

    @Test
    // Test V2 endpoint with no limit parameter
    // It should return 400 Status code
    public void TestV2NoLimit() {
        given().param("offset", 1).when().get("http://localhost:8080/v2/banks/all").then().statusCode(400);
    }

    @Test
    // Test V1 endpoint with valid offset and limit parameters
    // It should return size 10
    public void TestValidV1OffsetLimit() {
        given().param("offset", 0).param("limit", 10).when().get("http://localhost:8080/v1/banks/all").then().assertThat().body("size()", is(10));
    }

    @Test
    // Test V1 endpoint with valid offset and limit parameters
    // It should return size 20
    public void TestValidV2OffsetLimit() {
        given().param("offset", 0).param("limit", 20).when().get("http://localhost:8080/v2/banks/all").then().assertThat().body("size()", is(20));
    }


}