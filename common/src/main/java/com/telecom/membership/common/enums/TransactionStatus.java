package com.telecom.membership.common.enums;

public enum TransactionStatus {
    PENDING("처리 중입니다"),
    COMPLETED("포인트가 정상적으로 적립되었습니다"),
    FAILED("포인트 적립에 실패했습니다"),
    MAX_RETRY_EXCEEDED("최대 재시도 횟수를 초과했습니다");

    private final String message;

    TransactionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
