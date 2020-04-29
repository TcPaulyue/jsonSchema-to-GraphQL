FROM openjdk:8-jdk-alpine
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN apk add --no-cache maven
RUN mvn -B -f pom.xml clean package -DskipTests
ADD /workspace/target/*.jar app.jar
RUN touch /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]