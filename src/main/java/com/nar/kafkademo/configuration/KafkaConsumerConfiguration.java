package com.nar.kafkademo.configuration;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.constant.ConfigurationInfo;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.loginWithClientSecret;

//@EnableKafka
//@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private final KafkaUtil kafkaUtil;

    private ConsumerFactory<String, String> consumerClientFactory(ConfigurationInfo configurationInfo) throws IOException {
        // First, request access token using client id and secret
//        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, configurationInfo.getConsumerClient(), ConfigurationInfo.getClientSecret(configurationInfo.getConsumerClient()), true, null, null, true);

        Map<String, String> oauthConfig = new HashMap<>();
//        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, configurationInfo.getConsumerClient());
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, ConfigurationInfo.getClientSecret(configurationInfo.getConsumerClient()));
        oauthConfig.put(ClientConfig.OAUTH_TOKEN_ENDPOINT_URI, ApplicationConstant.KEYCLOAK_TOKEN_PATH);
        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");

        Properties consumerProps = kafkaUtil.buildConsumerConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);

        HashMap<String, Object> collect = consumerProps.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (prev, next) -> next, HashMap::new
                ));
        return new DefaultKafkaConsumerFactory<>(collect);
    }

    public ContainerCustomizer<String, String, ConcurrentMessageListenerContainer<String, String>> containerCustomizer(
            ConcurrentKafkaListenerContainerFactory<String, String> factory) {

        ContainerCustomizer<String, String, ConcurrentMessageListenerContainer<String, String>> cust = container -> container.getContainerProperties().setAuthExceptionRetryInterval(Duration.ofSeconds(5));
        factory.setContainerCustomizer(cust);
        return cust;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> consumerClient1ContainerFactory(@Qualifier("message1") KafkaTemplate<String, Object> kafkaTemplate) throws IOException {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerClientFactory(ConfigurationInfo.INFO_1));
        factory.setContainerCustomizer(containerCustomizer(factory));
        factory.setAutoStartup(true);

//        Alternatif
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (topic, ex) -> new TopicPartition(topic.topic() + ApplicationConstant.DLT_TOPIC_SUFFIX, topic.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000, 2)));

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> consumerClient2ContainerFactory() throws IOException {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setContainerCustomizer(containerCustomizer(factory));
        factory.setConsumerFactory(consumerClientFactory(ConfigurationInfo.INFO_2));
        factory.setAutoStartup(true);
        return factory;
    }
}
