// File: membership/common/src/main/java/com/telecom/membership/common/event/EventMessage.java
package com.telecom.membership.common.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMessage<T> {
    private String subject;
    private String eventType;
    private T data;
}