# FROM openjdk:8u111-jdk-alpine
# VOLUME /tmp
# ADD /target/*-exec.jar app.jar
# EXPOSE 9093
# ENTRYPOINT ["java","-Xmx256M","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=9093","-jar","/app.jar"]

FROM eclipse-temurin:11-jre
# SNAPSHOT 및 릴리즈 버전 공통 (definition-service-*.jar)
COPY definition-service/target/definition-service-*.jar definition.jar
COPY definition-service/src/main/resources/application.yml /app/application.yml
EXPOSE 8080
ENV DEFINITION_SERVER_PORT="8080"
ENV JAVA_OPTS="-Xmx400M -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS="kafka:29092,localhost:9092"
ENV SERVICE_HOST="localhost"
ENV UENGINE_DEFINITION_BASEPATH="/app"
ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BOOTSTRAP_SERVERS="kafka:29092,localhost:9092"
ENTRYPOINT exec java $JAVA_OPTS -Dserver.port=${DEFINITION_SERVER_PORT:-8080} -jar definition.jar --spring.config.location=/app/application.yml

# java -Xmx400M -Djava.security.egd=file:/dev/./urandom -Dserver.port=9093 -jar target/uengine-definition-1.1-SNAPSHOT-exec.jar --spring.profiles.active=default
