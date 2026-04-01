FROM eclipse-temurin:11-jre
# SNAPSHOT 및 릴리즈 버전 공통 (gateway-*.jar)
COPY target/gateway-*.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
# ENV KEYCLOAK_CLIENT_SERVER_URL=http://keycloak:9090
# ENV KEYCLOAK_CLIENT_REALM=uengine
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java","-Xmx400M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]
