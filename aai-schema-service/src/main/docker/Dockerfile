FROM eclipse-temurin:11-jre-alpine
USER nobody

WORKDIR /opt/app/aai-schema-service

# Expose the ports for outside linux to use
# 8447 is the important one to be used
EXPOSE 8447

COPY --chown=nobody:nobody /maven/aai-schema-service/ .

ENTRYPOINT ["/bin/sh", "/opt/app/aai-schema-service/docker-entrypoint.sh"]
