package com.nar.kafkademo.producer;

import com.nar.kafkademo.constant.ApplicationConstant;
import com.nar.kafkademo.constant.ConfigurationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/kafka-spring")
@RequiredArgsConstructor
@Slf4j
public class KafkaSpringProducer {

    @Qualifier("message1")
    private final KafkaTemplate<String, Object> kafkaTemplate1;

    private final AdminClient adminClient;

    //private final KafkaListenerEndpointRegistry registry;

    @PostMapping("/msg1")
//    @Transactional("producer1KafkaTransactionManager")
    public String sendMsg1() throws Exception {
        for (int i = 0; i < 1; i++) {
            String data = UUID.randomUUID().toString().toUpperCase();
            log.info("Sending to " + i + ".message " + ConfigurationInfo.INFO_1.getTopicName() + ": {}", data + i);
            String d = kafkaTemplate1.executeInTransaction(operations -> {
                operations.send(ConfigurationInfo.INFO_1.getTopicName(), data);
                return data;
            });
            Thread.sleep(500);
            if (i == 5) {
//                    throw new RuntimeException("Hata");
            }
        }

        ListTopicsResult result = adminClient.listTopics();

        Collection<TopicListing> list = result.listings().get();
        String topicName = null;

        for (TopicListing l : list) {
            System.out.println(l.name());
            topicName = l.name();
        }

        TopicPartition tp = new TopicPartition(topicName, 0);
        Map<TopicPartition, OffsetSpec> topicPartitionOffsets = Map.of(
                tp,
                OffsetSpec.latest()
        );
        var o = adminClient.listOffsets(topicPartitionOffsets);
        System.out.println(o.all().get().get(tp).offset());

        var cgs = adminClient.listConsumerGroups();

//            String data = UUID.randomUUID().toString().toUpperCase();
//            kafkaTemplate1.send(ConfigurationInfo.INFO_1.getTopicName(), data);
//            kafkaTemplate1.executeInTransaction(operations -> {
//                operations.send(ConfigurationInfo.INFO_1.getTopicName(), data);
//                return data;
//            });
//        throw new RuntimeException("Hata");
//            return data;
        return "success";
//        return "error";
    }

    @PostMapping("/msg2")
    public String get(@RequestBody Map<String, Boolean> map) {
        Boolean stop = map.get("stop");
        if (Boolean.TRUE.equals(stop)) {
            //registry.getListenerContainer("consumer1").stop();
        } else {
            //registry.getListenerContainer("consumer1").start();
        }
        return stop.toString();
    }
}
