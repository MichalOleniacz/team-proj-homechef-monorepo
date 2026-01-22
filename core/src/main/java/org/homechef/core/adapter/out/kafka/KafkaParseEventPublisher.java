package org.homechef.core.adapter.out.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.homechef.core.application.port.out.ParseEventPublisher;
import org.homechef.core.domain.recipe.ParseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class KafkaParseEventPublisher implements ParseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaParseEventPublisher.class);

    private final KafkaTemplate<String, ParseRequestEvent> kafkaTemplate;
    private final String topic;
    private final Counter publishSuccessCounter;
    private final Counter publishFailureCounter;

    public KafkaParseEventPublisher(
            KafkaTemplate<String, ParseRequestEvent> kafkaTemplate,
            @Value("${homechef.kafka.topic.parse-request:parse-requests}") String topic,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.publishSuccessCounter = meterRegistry.counter("kafka.publish", "topic", topic, "outcome", "success");
        this.publishFailureCounter = meterRegistry.counter("kafka.publish", "topic", topic, "outcome", "failure");
    }

    @Override
    public void publishParseRequest(ParseRequest parseRequest, String url) {
        ParseRequestEvent event = new ParseRequestEvent(
                parseRequest.getId(),
                url,
                parseRequest.getUrlHash().value(),
                Instant.now()
        );

        log.info("Publishing parse request event to Kafka",
                kv("requestId", event.requestId()),
                kv("urlHash", event.urlHash()),
                kv("topic", topic));

        CompletableFuture<SendResult<String, ParseRequestEvent>> future =
                kafkaTemplate.send(topic, event.urlHash(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                publishFailureCounter.increment();
                log.error("Failed to publish parse request event",
                        kv("requestId", event.requestId()),
                        kv("urlHash", event.urlHash()),
                        kv("topic", topic),
                        kv("error", ex.getMessage()));
            } else {
                publishSuccessCounter.increment();
                log.debug("Parse request event published successfully",
                        kv("requestId", event.requestId()),
                        kv("urlHash", event.urlHash()),
                        kv("topic", topic),
                        kv("partition", result.getRecordMetadata().partition()),
                        kv("offset", result.getRecordMetadata().offset()));
            }
        });
    }
}