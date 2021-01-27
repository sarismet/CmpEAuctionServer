FROM openjdk:15-jdk-alpine
ARG JAR_FILE=/build/libs/tr-1.0-SNAPSHOT.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","app.jar"]