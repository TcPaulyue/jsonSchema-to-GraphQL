FROM openjdk:8-jdk-alpine
RUN apk add --no-cache maven
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN mvn -B -f pom.xml clean package -DskipTests
ADD /workspace/target/querybuilder-0.0.1-SNAPSHOT.jar app.jar
RUN touch /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]