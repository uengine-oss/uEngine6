# FROM openjdk:8u111-jdk-alpine
# VOLUME /tmp
# ADD /target/*-exec.jar app.jar
# EXPOSE 9093
# ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=9093","-jar","/app.jar"]

FROM openjdk:8u212-jdk-alpine
ADD /target/*-exec.jar app.jar
EXPOSE 9093
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=9093","-jar","/app.jar"]

# java -Xmx400M -Djava.security.egd=file:/dev/./urandom -Dserver.port=9093 -jar target/uengine-definition-1.1-SNAPSHOT-exec.jar --spring.profiles.active=default