// File: membership/common/src/main/java/com/telecom/membership/common/exception/ErrorResponse.java
package com.telecom.membership.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
}
