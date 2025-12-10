FROM maven:3.9.6-eclipse-temurin-21 AS shared-build
WORKDIR /app/shared
COPY shared ./
RUN mvn clean install -DskipTests

FROM maven:3.9.6-eclipse-temurin-21 AS reader-build
WORKDIR /app/reader-service
COPY reader-service ./
COPY --from=shared-build /root/.m2 /root/.m2
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=reader-build /app/reader-service/target/*.jar app.jar
EXPOSE 8098
CMD ["java", "-jar", "app.jar"]