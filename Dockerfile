FROM openjdk:8-jdk-alpine
COPY . /app
WORKDIR /app
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/querybuilder-0.0.1-SNAPSHOT.jar"]
