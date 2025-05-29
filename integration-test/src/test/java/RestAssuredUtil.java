import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class RestAssuredUtil {
    private static final String BASE_URL = "http://localhost:4004";
    private static final String LOGIN_URL = "/auth/login";
    private static final String LOGIN_PAYLOAD =
          """
          {
            "email": "testuser@test.com",
            "password": "password123"
          }
        """;

    public static void initialize(){
        RestAssured.baseURI = BASE_URL;
    }

    public static String loginAndGetToken(){
        return given()
                .contentType(ContentType.JSON.toString())
                .body(LOGIN_PAYLOAD)
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response()
                .jsonPath()
                .getString("token");

    }

}
