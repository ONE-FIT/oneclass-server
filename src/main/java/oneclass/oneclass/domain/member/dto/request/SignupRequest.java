package oneclass.oneclass.domain.member.dto.request;

import jakarta.validation.constraints.*;
import oneclass.oneclass.domain.member.entity.Role;
import oneclass.oneclass.global.validation.PasswordMatches;

import java.util.List;

/**
 * username 기반 회원가입 요청 DTO
 * - 기존 phone 기반에서 username 기반으로 전환
 * - Parent 가입 시 자녀 식별자는 username 목록(studentId)로 받음
 * - Teacher/Student 가입 시 academyCode 필수(서비스에서 검증)
 * - Teacher 가입 시 verificationCode 필수(서비스에서 검증)
 * 주의: 서비스가 getXxx() 접근자를 사용하므로, record 컴포넌트 외에 브릿지 getter를 제공한다.
 */
@PasswordMatches(password = "password", confirm = "checkPassword")
public record SignupRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String checkPassword,
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.") String phone,
        @Email String email,
        @NotNull Role role,

        // 역할별 선택 입력(서비스에서 조건 검증)
        String verificationCode, // TEACHER 전용
        String academyCode,      // TEACHER/STUDENT 전용

        // PARENT 전용: 자녀 username 목록
        List<String> studentId
) {}