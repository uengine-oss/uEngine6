# FROM openjdk:8u111-jdk-alpine
# VOLUME /tmp
# ADD /target/*-exec.jar app.jar
# EXPOSE 9093
# ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=9093","-jar","/app.jar"]

FROM openjdk:11.0.11-jre-slim
COPY /target/*-SNAPSHOT.jar definition.jar
COPY src/main/resources/application.yml /app/application.yml
EXPOSE 8080
ENV JAVA_OPTS="-Xmx400M -Djava.security.egd=file:/dev/./urandom -Dserver.port=8080"
ENV SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS="kafka:29092,localhost:9092"
ENV SERVICE_HOST="localhost"
ENV UENGINE_DEFINITION_BASEPATH="/app"
ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BOOTSTRAP_SERVERS="kafka:29092,localhost:9092"
ENTRYPOINT exec java $JAVA_OPTS -jar definition.jar --spring.config.location=/app/application.yml

# java -Xmx400M -Djava.security.egd=file:/dev/./urandom -Dserver.port=9093 -jar target/uengine-definition-1.1-SNAPSHOT-exec.jar --spring.profiles.active=default
