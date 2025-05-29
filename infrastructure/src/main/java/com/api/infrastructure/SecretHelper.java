package com.api.infrastructure;

import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretHelper {

    private static final String DEFAULT_JWT_KEY = "q8JzbjcDB81PCIZZ7KerGCdqjxfzMOXYCuRODOlhoRg=";


    public static Secret createJwtSecret(Construct scope, String secretId, String secretName) {
        if(isLocalStackEnvironment()){
            return createLocalStackSecret(scope, secretId, secretName,
                    String.format("{\"jwtKey\":\"%s\"}", DEFAULT_JWT_KEY));
        }

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String base64Key = Base64.getEncoder().encodeToString(randomBytes);

        return Secret.Builder.create(scope, secretId)
                .secretName(secretName)
                .secretStringValue(SecretValue.unsafePlainText(
                        String.format("{\"jwtKey\":\"%s\"}", base64Key)
                ))
                .build();
    }


    private static boolean isLocalStackEnvironment() {
        return Boolean.parseBoolean(System.getenv("LOCALSTACK_ENABLED"))
                || System.getenv("AWS_ENDPOINT") != null;
    }

    private static Secret createLocalStackSecret(
            Construct scope,
            String secretId,
            String secretName,
            String defaultValue
    ) {

        return Secret.Builder.create(scope, secretId)
                .secretName(secretName)
                .secretStringValue(SecretValue.unsafePlainText(defaultValue))
                .build();
    }
}
