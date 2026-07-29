# ============================================================
# Multi-stage Dockerfile for marry-platform-admin
# ============================================================

# ---- build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml ./
COPY marry-platform-common        marry-platform-common
COPY marry-platform-api           marry-platform-api
COPY marry-platform-domain        marry-platform-domain
COPY marry-platform-persistence   marry-platform-persistence
COPY marry-platform-security      marry-platform-security
COPY marry-platform-system        marry-platform-system
COPY marry-platform-log           marry-platform-log
COPY marry-platform-monitor       marry-platform-monitor
COPY marry-platform-generator     marry-platform-generator
COPY marry-platform-admin         marry-platform-admin

RUN mvn -B -ntp -DskipTests -Pprod -pl marry-platform-admin -am package

# ---- run stage ----
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /src/marry-platform-admin/target/marry-platform-admin.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

USER spring:spring
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]