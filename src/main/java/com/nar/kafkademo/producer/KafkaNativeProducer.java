package com.nar.kafkademo.producer;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.loginWithClientSecret;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaNativeProducer {

    private final KafkaUtil kafkaUtil;

    @PostMapping("/producer")
    public String accessToken() throws IOException {
        final String clientId = "kafka-producer-client-2";
        final String clientSecret = clientId + "-secret";

        // First, request access token using client id and secret
        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, clientId, clientSecret, true, null, null, true);

        Map<String, String> oauthConfig = new HashMap<>();
        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");

        Properties producerProps = kafkaUtil.buildProducerConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);
        try (Producer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>(ApplicationConstant.TOPIC_NAME, ApplicationConstant.TOPIC_NAME)).get();
            return "json message sent successfully";
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "error";
    }

}