package oneclass.oneclass.domain.announce.service;

import oneclass.oneclass.domain.announce.error.AnnounceError;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Component
public class TimeValidator {

    private static final DateTimeFormatter STRICT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withResolverStyle(ResolverStyle.SMART);

    public LocalDateTime validateAndParse(String inputDateTime) {

        LocalDateTime parsedTime;

        // 1. [조건 1, 2] 형식 및 존재 여부 검증
        try {
            if (inputDateTime == null) {
                // null 체크를 명시적으로 추가
                throw new DateTimeParseException("Input string is null", "", 0);
            }
            parsedTime = LocalDateTime.parse(inputDateTime, STRICT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new CustomException(AnnounceError.ILLEGAL_TIME);
        }

        // 2. [조건 3] 현재 시간과 비교
        LocalDateTime minimumReservationTime = LocalDateTime.now().plusMinutes(30);
        if (!parsedTime.isAfter(minimumReservationTime)) { // isBefore() 또는 isEqual()
            throw new CustomException(AnnounceError.PAST_TIME);
        }

        // 3. 검증 성공
        return parsedTime;
    }
}