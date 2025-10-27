package oneclass.oneclass.domain.counsel.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConsultationStatus {
    REQUESTED,   // 신청됨
    CANCELLED,   // 취소됨
    CONFIRMED,   // 확정됨
    COMPLETED;   // 완료됨

    // "requested", "Requested" 같은 입력도 허용
    @JsonCreator
    public static ConsultationStatus from(String value) {
        if (value == null) return null;
        return ConsultationStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}