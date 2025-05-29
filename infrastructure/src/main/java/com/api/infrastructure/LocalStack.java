package com.api.infrastructure;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.Secret;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocalStack extends Stack {

    public LocalStack(final App app,final String id, final StackProps props) {
        super(app, id, props);

        Vpc vpc = InfrastructureFactory.createVpc("PatientsManagementVpc", this);
        InfrastructureFactory factory = new InfrastructureFactory(this, vpc);
        Cluster ecsCluster = factory.createEcsCluster(
                "PatientManagementCluster", "patients-management");

        DatabaseInstance patientsServiceDb = factory.createDatabaseInstance(
                "PatientsServiceDB","patients-service-db");
        DatabaseInstance authServiceDb = factory.createDatabaseInstance(
                "AuthServiceDB","auth-service-db");

        CfnHealthCheck patientsDbHealthCheck = factory.createHealthCheck(
                patientsServiceDb, "PatientsServiceDbHealthCheck");
        CfnHealthCheck authDbHealthCheck = factory.createHealthCheck(
                authServiceDb, "AuthServiceDbHealthCheck");

        CfnCluster mskCluster = factory.createMskCluster("kafka-cluster");

        Secret jwtSecretKey = Secret.fromSecretsManager(SecretHelper.createJwtSecret(this,
                "AuthJwtSecretKey",
                "auth/jwt"), "jwtKey");
        Secret authDbPassword = Secret.fromSecretsManager(Objects.requireNonNull(authServiceDb.getSecret()), "password");

        Secret patientsDbPassword = Secret.fromSecretsManager(Objects.requireNonNull(patientsServiceDb.getSecret()), "password");

        FargateService authService = factory.createFargateService(
                "AuthService",
                "auth-service",
                List.of(4003),
                authServiceDb,
                ecsCluster,
                Map.of("JWT_EXPIRATION_TIME","86400000"),
                Map.of("JWT_SECRET_KEY", jwtSecretKey,
                        "SPRING_DATASOURCE_PASSWORD", authDbPassword)
        );

        authService.getNode().addDependency(authDbHealthCheck);
        authService.getNode().addDependency(authServiceDb);

        FargateService billingService = factory.createFargateService(
                "BillingService",
                "billing-service",
                List.of(4001,9001),
                null,
                ecsCluster,
                null,
                null
        );

        FargateService analyticsService = factory.createFargateService(
                "AnalyticsService",
                "analytics-service",
                List.of(4002),
                null,
                ecsCluster,
                null,
                null
        );

        analyticsService.getNode().addDependency(mskCluster);

        FargateService patientsService = factory.createFargateService(
                "PatientService",
                "patients-service",
                List.of(4000),
                patientsServiceDb,
                ecsCluster,
                Map.of("BILLING_SERVICE_ADDRESS", "billing-service.patients-management",
                        "BILLING_SERVICE_GRPC_PORT", "9001"),
                Map.of("SPRING_DATASOURCE_PASSWORD", patientsDbPassword)
        );

        patientsService.getNode().addDependency(patientsServiceDb);
        patientsService.getNode().addDependency(patientsDbHealthCheck);
        patientsService.getNode().addDependency(billingService);
        patientsService.getNode().addDependency(mskCluster);

        factory.createApiGatewayService(
                "ApiGateway",
                "api-gateway",
                List.of(4004),
                null,
                ecsCluster,
                Map.of(
                        "SPRING_PROFILES_ACTIVE", "prod",
                        "AUTH_SERVICE_URL", "http://auth-service.patients-management:4003/auth",
                        "LOGGING_LEVEL_ROOT", "DEBUG",
                        "LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY", "TRACE"
                ),
                null
        );
    }


    public static void main(final String[] args) {
        App app = new App(AppProps.builder()
                .outdir("./cdk.out")
                .build());

        StackProps stackProps = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app, "localstack", stackProps);
        app.synth();
        System.out.println("App starting in progress");
    }

}
