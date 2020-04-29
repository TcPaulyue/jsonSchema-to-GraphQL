FROM openjdk:8-jdk-alpine
RUN apk add --no-cache maven
RUN mvn -B -f pom.xml clean package -DskipTests
ADD /target/*.jar app.jar
RUN touch /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]