# FROM jboss/keycloak
# COPY /standalone/configuration/standalone.xml /opt/jboss/keycloak/standalone/configuration/
# # CMD [ "/opt/jboss/keycloak/bin/standalone.sh" ]
# EXPOSE 9090
# ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]
# # ENTRYPOINT [ "/opt/jboss/keycloak/standalone/configuration/docker-standalone.sh" ]

FROM jboss/keycloak
COPY /standalone/configuration/standalone.xml /opt/jboss/keycloak/standalone/configuration/
EXPOSE 8080
ENV JBOSS_HOME=/opt/jboss/keycloak
ENV KEYCLOAK_VERSION=15.0.2
ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]