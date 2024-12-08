// File: membership/common/src/main/java/com/telecom/membership/common/service/EventGridService.java
package com.telecom.membership.gateway.service;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.membership.common.event.EventMessage;
import com.telecom.membership.common.exception.PointException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventGridService {
    private final EventGridPublisherClient client;
    private final ObjectMapper objectMapper;

    public <T> Mono<Void> publishEvent(EventMessage<T> message) {
        return Mono.fromRunnable(() -> {
            try {
                EventGridEvent event = createEvent(message);
                client.sendEvent(event);
                log.debug("Published event: {}", message);
            } catch (Exception e) {
                log.error("Failed to publish event: {}", e.getMessage(), e);
                throw new PointException.EventPublishException(e);
            }
        });
    }

    private <T> EventGridEvent createEvent(EventMessage<T> message) {
        try {
            String jsonData = objectMapper.writeValueAsString(message.getData());
            return new EventGridEvent(
                    message.getSubject(),
                    message.getEventType(),
                    BinaryData.fromString(jsonData),
                    "1.0"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event data", e);
        }
    }
}