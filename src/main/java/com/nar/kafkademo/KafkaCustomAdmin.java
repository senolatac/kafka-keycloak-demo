package com.nar.kafkademo;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.constant.ConfigurationInfo;
import com.nar.kafkademo.utils.KafkaUtil;
import io.strimzi.kafka.oauth.client.ClientConfig;
import io.strimzi.kafka.oauth.common.Config;
import io.strimzi.kafka.oauth.common.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.strimzi.kafka.oauth.common.OAuthAuthenticator.loginWithClientSecret;
import static java.util.Collections.singletonList;

@RestController
@RequestMapping("/kafka-admin")
@Slf4j
@RequiredArgsConstructor
public class KafkaCustomAdmin {

    private final KafkaUtil kafkaUtil;
    @GetMapping("/topics")
    public String getTopics() throws ExecutionException, InterruptedException, IOException {
        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, "kafka-admin-client", "rO8Ns8H8wnh3ocgHWZcmNaAQR3Iu4bJV", true, null, null, true);

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
        Set<String> strings = null;
        try (AdminClient adminClient = AdminClient.create(collect)) {
            //
            // Create x_* topic
            //
            ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
            listTopicsOptions.listInternal(true);
            strings = adminClient.listTopics(listTopicsOptions).names().get();
        }



        return "topics:" + strings;
    }

//    @PostMapping("/create-topic")
//    public String createTopic() throws IOException, ExecutionException, InterruptedException {
//
//        TokenInfo info = loginWithClientSecret(URI.create(ApplicationConstant.KEYCLOAK_TOKEN_PATH), null, null, "kafka-admin-client", "rO8Ns8H8wnh3ocgHWZcmNaAQR3Iu4bJV", true, null, null, true);
//
//        Map<String, String> oauthConfig = new HashMap<>();
//        oauthConfig.put(ClientConfig.OAUTH_ACCESS_TOKEN, info.token());
//        oauthConfig.put(ClientConfig.OAUTH_CLIENT_ID, "kafka");
//        oauthConfig.put(ClientConfig.OAUTH_CLIENT_SECRET, "kafka-secret");
//        oauthConfig.put(Config.OAUTH_USERNAME_CLAIM, "preferred_username");
//
//        Properties producerProps = kafkaUtil.buildAdminClientConfigOAuthBearer(ApplicationConstant.KAFKA_INTROSPECT_SERVER_CONFIG, oauthConfig);
//
//        HashMap<String, Object> collect = producerProps.entrySet().stream().collect(
//                Collectors.toMap(
//                        e -> String.valueOf(e.getKey()),
//                        Map.Entry::getValue,
//                        (prev, next) -> next, HashMap::new
//                ));
//
//
//        try (AdminClient admin = AdminClient.create(collect)) {
//            admin.createTopics(singletonList(new NewTopic(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1, 2, (short) 1))).all().get();
//            admin.createTopics(singletonList(new NewTopic(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1 + ApplicationConstant.DLT_TOPIC_SUFFIX, 2, (short) 1))).all().get();
//            admin.createTopics(singletonList(new NewTopic(ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2, 2, (short) 1))).all().get();
//        }
//
//        return "Success";
//    }
}
