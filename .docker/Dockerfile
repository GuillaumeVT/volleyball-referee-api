FROM gradle:8.10.2-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle bootJar

FROM amazoncorretto:21.0.5-alpine3.20

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]