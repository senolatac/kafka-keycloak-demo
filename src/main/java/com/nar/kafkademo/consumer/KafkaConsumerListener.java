package com.nar.kafkademo.consumer;

import com.nar.kafkademo.DemoRepository;
import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.constant.ConfigurationInfo;
import com.nar.kafkademo.model.DemoEntity;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerListener {


    @Qualifier("message2")
    private final KafkaTemplate<String, Object> kafkaTemplate;
    //private final DemoRepository demoRepository;

    @KafkaListener(id = "consumer1", topics = ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1,
            containerFactory = ApplicationConstant.CONSUMER_CLIENT_1_CONTAINER_FACTORY,
            groupId = "g" + ApplicationConstant.ID_1 + "group", autoStartup = "true")
//    @RetryableTopic(
//            kafkaTemplate = "message1",
//            timeout = "2000",
//            backoff = @Backoff(delay = 1000, multiplier = 2.0),
//            autoCreateTopics = "false",
//            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    // TODO: Kafka Manager yazıldığında exception halinde veritabanına yazıyor. Ama Kafka'ya yazmıyor.
    @Transactional("producer1KafkaTransactionManager")
    public void consumerClient1(String message) throws Exception {
        log.info("Received from " + ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1 + ": {}", message);
//        log.info("Sending to " + ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2 + ": {}", message);

//        kafkaTemplate.send(ConfigurationInfo.INFO_2.getTopicName(), message);
//        kafkaTemplate.executeInTransaction(operations -> operations.send(ConfigurationInfo.INFO_2.getTopicName(), message));
        log.info("Writing to database: {}", message);
        /*demoRepository.save(
                DemoEntity.builder()
                        .name(message)
                        .timestamp(System.currentTimeMillis())
                        .build()
        );
        sendToKafka(message);*/
//        throw new RuntimeException("Hata");
    }

//    @Transactional("producer2KafkaTransactionManager")
    public void sendToKafka(String message) {
        this.kafkaTemplate.send(ConfigurationInfo.INFO_2.getTopicName(), message);
//        throw new RuntimeException("Hata");
    }


    @KafkaListener(id = "consumer2", topics = ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2,
            containerFactory = ApplicationConstant.CONSUMER_CLIENT_2_CONTAINER_FACTORY,
            groupId = "g" + ApplicationConstant.ID_2 + "group")
    public void consumerClient2(String message) {
        log.info("Received from " + ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_2 + ": {}", message);
    }

//    @DltHandler
//    public void dltHandler(String message) {
//        log.info("DLT -> " + message);
//    }

    @KafkaListener(topics = ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1 + ApplicationConstant.DLT_TOPIC_SUFFIX,
            containerFactory = ApplicationConstant.CONSUMER_CLIENT_1_CONTAINER_FACTORY,
            groupId = "g" + ApplicationConstant.ID_1 + "group")
    public void consumerClient1Dlt(String message) {
        log.info("Received from " + ApplicationConstant.TOPIC_NAME + ApplicationConstant.ID_1 + ApplicationConstant.DLT_TOPIC_SUFFIX + ": {}", message);
    }

}
