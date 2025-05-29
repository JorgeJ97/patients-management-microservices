FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw ./

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

COPY --from=builder /build/target/billing-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4001
EXPOSE 9001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


