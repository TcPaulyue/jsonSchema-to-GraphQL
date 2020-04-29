FROM openjdk:8-jre-alpine
VOLUME /tmp
ARG DOCKERIZE_VERSION=v0.6.1

ADD target/*.jar /app.jar
RUN touch /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]