FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

# Add the proper files into the docker image from your build
WORKDIR /opt/app/aai-schema-service

# Expose the ports for outside linux to use
# 8447 is the important one to be used
EXPOSE 8447

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD nc -z -v localhost 8452 || exit 1

ENTRYPOINT ["/bin/bash", "/opt/app/aai-schema-service/docker-entrypoint.sh"]

VOLUME /tmp
VOLUME /opt/tools

USER root

COPY --chown=aaiadmin:aaiadmin /maven/aai-schema-service/ /opt/app/aai-schema-service

RUN  mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-SS && \
     chown -R aaiadmin:aaiadmin /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-SS && \
     chmod 777 /opt/app/aai-schema-service /opt/app/aai-schema-service/resources/etc/auth


ENV AAI_BUILD_VERSION @aai.docker.version@

WORKDIR /opt/app/aai-schema-service

USER aaiadmin
