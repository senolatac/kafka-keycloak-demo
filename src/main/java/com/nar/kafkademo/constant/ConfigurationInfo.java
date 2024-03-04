package com.nar.kafkademo.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigurationInfo {
    INFO_1(ApplicationConstant.KAFKA_PRODUCER_CLIENT + ApplicationConstant.ID_1,
            ApplicationConstant.KAFKA_CONSUMER_CLIENT + ApplicationConstant.ID_1,
            ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1, "g" + ApplicationConstant.ID_1 + "group"),
    INFO_2(ApplicationConstant.KAFKA_PRODUCER_CLIENT + ApplicationConstant.ID_2,
            ApplicationConstant.KAFKA_CONSUMER_CLIENT + ApplicationConstant.ID_2,
            ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2, "g" + ApplicationConstant.ID_2 + "group");

    private final String producerClient;
    private final String consumerClient;
    private final String topicName;
    private final String groupId;

    public static String getClientSecret(String client) {
        //return client + "-secret";
        return "KpT9tpDZXKM90eINGhzS5vHNPd9zOp3b";
    }
}
