import static io.restassured.RestAssured.given;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AuthIntegrationTest {

    @BeforeAll
    static void setUpBefore() {
        RestAssuredUtil.initialize();
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        String token = RestAssuredUtil.loginAndGetToken();

        System.out.println("Generated Token: " + token);
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        String loginPayload = """
          {
            "email": "invalid_user@test.com",
            "password": "wrongpassword"
          }
        """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }
}
