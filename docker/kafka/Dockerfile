FROM quay.io/strimzi/kafka:0.35.1-kafka-3.4.0

USER root
RUN rm -rf /opt/kafka/libs/bcpkix* /opt/kafka/libs/bcprov* /opt/kafka/libs/keycloak*
USER kafka
