import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;


public class PatientsIntegrationTest {

    @BeforeAll
    public static void setUpBefore() {
        RestAssuredUtil.initialize();

    }

    @Test
    public void getPatientsWithTokenTest() {
        String token = RestAssuredUtil.loginAndGetToken();


       Response response =  given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(ContentType.JSON.toString())
                .when()
                .get("/api/patients/get-patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue())
                .extract()
                .response();

       System.out.println(response.getBody().prettyPeek());
    }
}
