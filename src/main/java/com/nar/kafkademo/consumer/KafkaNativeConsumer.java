package com.nar.kafkademo.consumer;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.loginWithClientSecret;
import static java.util.Collections.singletonList;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaNativeConsumer {

    private final KafkaUtil kafkaUtil;

//    @GetMapping("/consumer")
//    public String accessToken() throws IOException {
//        String s = "s";
//        final String clientId = "kafka-consumer-client-1";
//        final String clientSecret = clientId + "-secret";
//
//        // First, request access token using client id and secret
//        TokenInfo info = getInfo(clientId, clientSecret);
//
//        Map<String, String> oauthConfig = new HashMap<>();
//        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
//        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, "kafka");
//        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, "kafka-secret");
//        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");
//
//        Properties consumerProps = kafkaUtil.buildConsumerConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);
//        try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
//            TopicPartition partition = new TopicPartition(ApplicationConstant.TOPIC_MSG_1, 0);
//            consumer.assign(singletonList(partition));
//
//            while (consumer.partitionsFor(ApplicationConstant.TOPIC_MSG_1, Duration.ofSeconds(1)).size() == 0) {
//                log.error("No assignment yet for consumer");
//            }
//            consumer.seekToBeginning(singletonList(partition));
//
//            ConsumerRecords<String, String> records = kafkaUtil.poll(consumer);
//
//            s = "Consumed --> " + (records.count() > 1 ? records.iterator().next().value() : "Boş");
//            log.info(s);
//
//        }
//        return s;
//    }
//
//    @Cacheable(value = "token")
//    public TokenInfo getInfo(String clientId, String clientSecret) throws IOException {
//        return loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, clientId, clientSecret, true, null, null, true);
//    }
}
