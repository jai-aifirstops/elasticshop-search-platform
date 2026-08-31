package com.jaideep.ecommerce.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ProductEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public ProductEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.product-topic}")
            String topic
    ) {
        this.kafkaTemplate =
                kafkaTemplate;

        this.topic =
                topic;
    }

    public void publishUpsert(
            Long documentId
    ) {

        publish(
                "UPSERT",
                documentId
        );
    }

    public void publishDelete(
            Long documentId
    ) {

        publish(
                "DELETE",
                documentId
        );
    }

    private void publish(
            String operation,
            Long documentId
    ) {

        String payload =
                operation
                        + "|"
                        + documentId;

        try {

            kafkaTemplate
                    .send(
                            topic,
                            documentId.toString(),
                            payload
                    )
                    .get(
                            10,
                            TimeUnit.SECONDS
                    );

        }
        catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to publish Kafka event",
                    exception
            );
        }
    }
}