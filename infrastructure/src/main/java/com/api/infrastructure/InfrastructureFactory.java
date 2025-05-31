package com.api.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfrastructureFactory {
    private final Stack scope;
    private final Vpc vpc;

    public InfrastructureFactory(final Stack scope, Vpc vpc) {
        this.vpc = vpc;
        this.scope = scope;
    }

    public  static Vpc createVpc(String vpcName, Stack scope) {
        return Vpc.Builder
                .create(scope, vpcName)
                .vpcName(vpcName)
                .maxAzs(2)
                .build();
    }

    public DatabaseInstance createDatabaseInstance(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(scope, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17)
                                .build()))
                .vpc(vpc)
                .databaseName(dbName)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public CfnHealthCheck createHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder
                .create(scope, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build()
                )
                .build();
    }

    public CfnCluster createMskCluster(String clusterName) {
        return CfnCluster.Builder
                .create(scope, "MskCluster")
                .clusterName(clusterName)
                .kafkaVersion("3.6.0")
                .numberOfBrokerNodes(2)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.large")
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId).toList())
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }


    public Cluster createEcsCluster(String clusterName, String cloudMapNamespace) {
        return Cluster.Builder.create(scope, clusterName)
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name(cloudMapNamespace)
                        .build())
                .build();
    }



    public FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Cluster ecsCluster,
            Map<String, String> additionalEnvVars,
            Map<String, Secret> secrets
            ){
        var taskDefinition = getTaskDefinition(id,imageName,ports,db,additionalEnvVars,secrets);

        return FargateService.Builder
                .create(scope, id)
                .cluster(ecsCluster)
                .serviceName(imageName)
                .assignPublicIp(false)
                .taskDefinition(taskDefinition)
                .build();
    }

    public void createApiGatewayService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Cluster ecsCluster,
            Map<String, String> additionalEnvVars,
            Map<String, Secret> secrets
    ){
        var taskDefinition = getTaskDefinition(id,imageName,ports,db,additionalEnvVars,secrets);

        ApplicationLoadBalancedFargateService.Builder
                .create(scope,id)
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }

    private FargateTaskDefinition getTaskDefinition(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars,
            Map<String, Secret> secrets
            ) {
        var taskDefinition = createFargateTaskDefinition(id);
        var containerOptions = createContainerDefinitionOptions(id,ports, imageName);
        var envVars = getEnvVars(additionalEnvVars, db, imageName);

        if(secrets != null && !secrets.isEmpty()){
            containerOptions.secrets(secrets);
        }

        containerOptions.environment(envVars);
        taskDefinition.addContainer(id + "Container", containerOptions.build());

        return taskDefinition;

    }


    private FargateTaskDefinition createFargateTaskDefinition(String id){
        return FargateTaskDefinition.Builder
                .create(scope, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();
    }

    private ContainerDefinitionOptions.Builder createContainerDefinitionOptions(
            String id, List<Integer>ports, String imageName){
        return ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream().map(port ->PortMapping.builder()
                        .containerPort(port)
                        .hostPort(port)
                        .protocol(Protocol.TCP)
                        .build()).toList())
                .logging(LogDriver.awsLogs((AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(scope, id + "LogGroup")
                                .logGroupName("/ecs/" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix(imageName)
                        .build())));

    }

    private Map<String, String> getEnvVars(
            Map<String, String> additionalEnvVars,
            DatabaseInstance db,
            String imageName){

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

        if(additionalEnvVars != null){
            envVars.putAll(additionalEnvVars);
        }

        if(db != null){
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");

        }
        return envVars;
    }
}
