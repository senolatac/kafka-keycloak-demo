package com.nar.kafkademo.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApplicationConstant {

    public static final String ID_1 = "1";
    public static final String ID_2 = "2";
    public static final String KEYCLOAK_HOST = "localhost:8080";
    public static final String KEYCLOAK_REALM = "yeap-simulation";
    public static final String KEYCLOAK_TOKEN_PATH = "http://" + KEYCLOAK_HOST + "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token";
    public static final String KAFKA_INTROSPECT_SERVER_CONFIG = "localhost:9092";
    public static final String TOPIC_NAME = "messages-";
    public static final String CONSUMER_CLIENT_1_CONTAINER_FACTORY = "consumerClient" + ID_1 + "ContainerFactory";
    public static final String CONSUMER_CLIENT_2_CONTAINER_FACTORY = "consumerClient" + ID_2 + "ContainerFactory";

    public static final String KAFKA_PRODUCER_CLIENT = "kafka-producer-client-";
    public static final String KAFKA_CONSUMER_CLIENT = "kafka-consumer-client-";
    public static final String DLT_TOPIC_SUFFIX = "-dlt";
}
