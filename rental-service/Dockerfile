FROM maven:3.9.6-eclipse-temurin-21 AS shared-build
WORKDIR /app/shared
COPY shared ./
RUN mvn clean install -DskipTests

FROM maven:3.9.6-eclipse-temurin-21 AS rental-build
WORKDIR /app/rental-service
COPY rental-service ./
COPY --from=shared-build /root/.m2 /root/.m2
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=rental-build /app/rental-service/target/*.jar app.jar
EXPOSE 8099
CMD ["java", "-jar", "app.jar"]