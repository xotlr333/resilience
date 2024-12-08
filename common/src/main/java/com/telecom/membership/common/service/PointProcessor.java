// File: membership/common/src/main/java/com/telecom/membership/common/service/PointProcessor.java
package com.telecom.membership.common.service;

import com.telecom.membership.common.dto.PointRequest;
import com.telecom.membership.common.dto.PointResponse;
import reactor.core.publisher.Mono;

public interface PointProcessor {
    Mono<PointResponse> processPoints(PointRequest request);
}