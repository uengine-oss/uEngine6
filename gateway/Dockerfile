FROM openjdk:8u212-jdk-alpine
COPY target/*SNAPSHOT.jar app.jar
ARG CACHEBUST=202111051111
EXPOSE 8088
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=8088","-jar","/app.jar"]
