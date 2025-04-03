FROM openjdk:11-jdk-slim
COPY target/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
# ENV KEYCLOAK_CLIENT_SERVER_URL=http://keycloak:9090
# ENV KEYCLOAK_CLIENT_REALM=uengine
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]
