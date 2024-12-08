// File: membership/common/src/main/java/com/telecom/membership/common/response/ApiResponse.java
package com.telecom.membership.common.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .build();
    }
}
