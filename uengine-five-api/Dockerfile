FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD /target/*-exec.jar app.jar
EXPOSE 9095
ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=9095","-jar","/app.jar"]