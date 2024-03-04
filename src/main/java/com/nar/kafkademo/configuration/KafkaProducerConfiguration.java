package com.nar.kafkademo.configuration;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.constant.ConfigurationInfo;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import javax.sql.DataSource;
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
public class KafkaProducerConfiguration {

    private final KafkaUtil kafkaUtil;

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

    @Bean(name = "message1")
    public KafkaTemplate<String, Object> kafka1Template() throws IOException {
        return new KafkaTemplate<>(producerFactory(ConfigurationInfo.INFO_1));
    }

    @Bean(name = "message2")
    public KafkaTemplate<String, Object> kafka2Template() throws IOException {
        return new KafkaTemplate<>(producerFactory(ConfigurationInfo.INFO_2));
    }

    private ProducerFactory<String, Object> producerFactory(ConfigurationInfo configurationInfo) throws IOException {

        // First, request access token using client id and secret
//        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, configurationInfo.getProducerClient(), ConfigurationInfo.getClientSecret(configurationInfo.getProducerClient()), true, null, null, true);

        Map<String, String> oauthConfig = new HashMap<>();
//        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, configurationInfo.getProducerClient());
        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, ConfigurationInfo.getClientSecret(configurationInfo.getProducerClient()));
        oauthConfig.put(ClientConfig.OAUTH_TOKEN_ENDPOINT_URI, ApplicationConstant.KEYCLOAK_TOKEN_PATH);
        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");

        Properties producerProps = kafkaUtil.buildProducerConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);

        HashMap<String, Object> collect = producerProps.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (prev, next) -> next, HashMap::new
                ));

        return new DefaultKafkaProducerFactory<>(collect);
    }

    @Bean(name = "producer1KafkaTransactionManager")
    public KafkaTransactionManager<String, Object> producer1KafkaTransactionManager() throws IOException {
        KafkaTransactionManager<String, Object> kafkaTransactionManager = new KafkaTransactionManager<>(kafka1Template().getProducerFactory());
        kafkaTransactionManager.setTransactionIdPrefix("tx-");
        kafkaTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
        return kafkaTransactionManager;
    }
//
//    @Bean(name = "producer2KafkaTransactionManager")
//    public KafkaTransactionManager<String, Object> producer2KafkaTransactionManager() throws IOException {
//        KafkaTransactionManager<String, Object> kafkaTransactionManager = new KafkaTransactionManager<>(kafka2Template().getProducerFactory());
//        kafkaTransactionManager.setTransactionIdPrefix("tx-");
//        kafkaTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
//        return kafkaTransactionManager;
//    }
}
