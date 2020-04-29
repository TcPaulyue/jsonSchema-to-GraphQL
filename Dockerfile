FROM openjdk:8-jre-alpine
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN apk add --no-cache maven
RUN mvn -B -f pom.xml clean package -DskipTests
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]