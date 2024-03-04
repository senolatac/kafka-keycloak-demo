package com.nar.kafkademo.configuration;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.loginWithClientSecret;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaAdminClientConfiguration {

    private final KafkaUtil kafkaUtil;

    @Bean
    public KafkaAdmin adminClient() throws IOException {

        // First, request access token using client id and secret
        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, "kafka-admin-client", "QrL8rwC2Dsmh4cXzkBCbCuOfc1VLQ0nF", true, null, null, true);

        Map<String, String> oauthConfig = new HashMap<>();
        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, "kafka");
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, "kafka-secret");
        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");

        Properties producerProps = kafkaUtil.buildAdminClientConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);

        HashMap<String, Object> collect = producerProps.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (prev, next) -> next, HashMap::new
                ));

        return new KafkaAdmin(collect);
    }

    @Bean
    public AdminClient adminClientConfig() throws IOException {
        return AdminClient.create(adminClient().getConfigurationProperties());
    }

    @Bean
    public NewTopic message1Topic() {
        return TopicBuilder.name(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1)
                .partitions(1)
                .build();
    }

    //@Bean
    public NewTopic message1DltTopic() {
        return TopicBuilder.name(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1 + ApplicationConstant.DLT_TOPIC_SUFFIX)
                .partitions(2)
                .build();
    }

    //@Bean
    public NewTopic message2Topic() {
        return TopicBuilder.name(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2)
                .partitions(2)
                .build();
    }
}
