FROM openjdk:8-jdk-alpine as mvn-build
ARG CACHEBUST=202111051111
WORKDIR /app/build
COPY ./src ./src
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw clean install -Dmaven.test.skip=true

FROM openjdk:8-jre-alpine
EXPOSE 9091
WORKDIR /app
COPY --from=mvn-build /app/build/target/*.jar ./spring-app.jar
CMD ["java", "-jar", "/app/spring-app.jar"]


# FROM openjdk:8u212-jdk-alpine
# ADD /target/*-exec.jar app.jar
# EXPOSE 8080
# ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=8080","-jar","/app.jar"]
