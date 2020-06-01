FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

RUN  mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-SCHEMA-SERVICE /opt/app/aai-schema-service/logs/gc

VOLUME /opt/aai/logroot/AAI-SS
VOLUME /tmp
VOLUME /opt/tools

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD nc -z -v localhost 8452 || exit 1

RUN groupadd aaiadmin -g 1000

RUN adduser -u 1000 -h /opt/aaihome/aaiadmin -S -D -G aaiadmin -s /bin/bash aaiadmin

# Add the proper files into the docker image from your build
WORKDIR /opt/app/aai-schema-service

RUN chown -R aaiadmin:aaiadmin /opt/app/aai-schema-service /etc/profile.d /opt/aai/logroot/AAI-SS /opt/app /opt/aai/logroot /opt/app/aai-schema-service/logs/gc

COPY --chown=aaiadmin:aaiadmin /maven/aai-schema-service/ .

USER aaiadmin

ENV AAI_BUILD_VERSION @aai.docker.version@
# Expose the ports for outside linux to use
# 8452 is the important one to be used
EXPOSE 8452
ENTRYPOINT ["/bin/bash", "/opt/app/aai-schema-service/docker-entrypoint.sh"]
