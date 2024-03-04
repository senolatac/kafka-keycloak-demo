package com.nar.kafkademo;

import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.AuthenticationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest
class CustomCheckTests {

    @Autowired
    private KafkaUtil kafkaUtil;

    @Test
    void checkKafka() throws InterruptedException, ExecutionException, IOException {
        final String kafkaBootstrap = "kafka:9098";
        final String hostPort = "keycloak:8080";
        final String realm = "kafka-authz";

        final String tokenEndpointUri = "http://" + hostPort + "/realms/" + realm + "/protocol/openid-connect/token";

        // logging in as 'team-b-client' should succeed - iss check, clientId check, aud check, resource_access check should all pass

        Map<String, String> oauthConfig = new HashMap<>();
        oauthConfig.put(ClientConfig.OAUTH_TOKEN_ENDPOINT_URI, tokenEndpointUri);
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, "team-b-client");
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, "team-b-client-secret");
        oauthConfig.put(ClientConfig.OAUTH_USERNAME_CLAIM, "preferred_username");

        Properties producerProps = kafkaUtil.buildProducerConfigOAuthBearer(kafkaBootstrap, oauthConfig);
        Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(producerProps);

        final String topic = "KeycloakAuthenticationTest-customClaimCheckWithJwtTest";


        producer.send(new ProducerRecord<>(topic, "The Message")).get();
        log.debug("Produced The Message");


        // logging in as 'bob' should fail - clientId check, aud check and resource_access check would all fail
        String token = kafkaUtil.loginWithUsernamePassword(URI.create(tokenEndpointUri), "bob", "bob-password", "kafka-cli");

        oauthConfig = new HashMap<>();
        oauthConfig.put(ClientConfig.OAUTH_TOKEN_ENDPOINT_URI, tokenEndpointUri);
        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, token);
        oauthConfig.put(ClientConfig.OAUTH_USERNAME_CLAIM, "preferred_username");

        producerProps = kafkaUtil.buildProducerConfigOAuthBearer(kafkaBootstrap, oauthConfig);
        producer = new KafkaProducer<>(producerProps);

        try {
            producer.send(new ProducerRecord<>(topic, "Bob's Message")).get();
            Assertions.fail("Producing the message should have failed");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            Assertions.assertTrue(cause instanceof AuthenticationException, "instanceOf AuthenticationException");
            Assertions.assertTrue(cause.toString().contains("Custom claim check failed"), "custom claim check failed");
        }
    }
}
