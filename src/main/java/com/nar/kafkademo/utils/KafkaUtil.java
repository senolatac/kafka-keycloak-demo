package com.nar.kafkademo.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.strimzi.kafka.oauth.common.HttpUtil;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.base64encode;
import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.urlencode;

@Component
public class KafkaUtil {

    public Properties buildAdminClientConfigOAuthBearer(String kafkaBootstrap, Map<String, String> oauthConfig) {
        Properties p = buildCommonConfigOAuthBearer(oauthConfig);
        setCommonAdminClientProperties(kafkaBootstrap, p);
        return p;
    }

    public void setCommonAdminClientProperties(String kafkaBootstrap, Properties p) {
        p.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
        p.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        p.setProperty(AdminClientConfig.RETRIES_CONFIG, "0");
    }

    public Properties buildProducerConfigOAuthBearer(String kafkaBootstrap, Map<String, String> oauthConfig) {
        Properties p = buildCommonConfigOAuthBearer(oauthConfig);
        setCommonProducerProperties(kafkaBootstrap, p);
        return p;
    }

    public void setCommonProducerProperties(String kafkaBootstrap, Properties p) {
        p.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
        p.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        p.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        p.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        p.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
        p.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
        p.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "2");
        p.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");

        // To transactional producer
        p.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        p.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-");

        // To ease debugging
        p.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
    }

    public Properties buildConsumerConfigOAuthBearer(String kafkaBootstrap, Map<String, String> oauthConfig) {
        Properties p = buildCommonConfigOAuthBearer(oauthConfig);
        setCommonConsumerProperties(kafkaBootstrap, p);
        return p;
    }

    public void setCommonConsumerProperties(String kafkaBootstrap, Properties p) {
        p.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
        p.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        p.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        p.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));

        // To ease debugging
        p.setProperty(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "600000");
    }

    public Properties buildCommonConfigOAuthBearer(Map<String, String> oauthConfig) {
        String configOptions = getJaasConfigOptionsString(oauthConfig);

        Properties p = new Properties();
        p.setProperty("security.protocol", "SASL_PLAINTEXT");
        p.setProperty("sasl.mechanism", "OAUTHBEARER");
        p.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required " + configOptions + " ;");
        p.setProperty("sasl.login.callback.handler.class", "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");

        return p;
    }

    public String getJaasConfigOptionsString(Map<String, String> options) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> ent : options.entrySet()) {
            sb.append(" ").append(ent.getKey()).append("=\"").append(ent.getValue()).append("\"");
        }
        return sb.toString();
    }

    public <K, V> ConsumerRecords<K, V> poll(Consumer<K, V> consumer) {
        ConsumerRecords<K, V> result = consumer.poll(Duration.ofSeconds(5));
        if (result.isEmpty()) {
            result = consumer.poll(Duration.ofSeconds(5));
        }
        return result;
    }

    public String loginWithUsernamePassword(URI tokenEndpointUri, String username, String password, String clientId) throws IOException {
        return loginWithUsernamePassword(tokenEndpointUri, username, password, clientId, null);
    }

    public String loginWithUsernamePassword(URI tokenEndpointUri, String username, String password, String clientId, String secret) throws IOException {

        String body = "grant_type=password&username=" + urlencode(username) +
                "&password=" + urlencode(password);

        String authorization = "Basic " + base64encode(clientId + ":" + (secret != null ? secret : ""));

        JsonNode result = HttpUtil.post(tokenEndpointUri,
                null,
                null,
                authorization,
                "application/x-www-form-urlencoded",
                body,
                JsonNode.class);

        JsonNode token = result.get("access_token");
        if (token == null) {
            throw new IllegalStateException("Invalid response from authorization server: no access_token");
        }
        return token.asText();
    }

}
