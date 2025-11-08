package oneclass.oneclass.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String passwordAccessor;
    private String confirmAccessor;
    private String message;

    @Override
    public void initialize(PasswordMatches annotation) {
        this.passwordAccessor = annotation.password();
        this.confirmAccessor = annotation.confirm();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull 등이 별도로 처리
        try {
            Method pwdGetter = value.getClass().getMethod(passwordAccessor);
            Method confirmGetter = value.getClass().getMethod(confirmAccessor);

            Object pwd = pwdGetter.invoke(value);
            Object confirm = confirmGetter.invoke(value);

            if (pwd == null || confirm == null) return true; // 각 필드는 @NotBlank 등으로 처리

            boolean matched = pwd.equals(confirm);
            if (!matched) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            }
            return matched;
        } catch (Exception e) {
            // 접근자 메서드명이 잘못된 경우 등: 실패 처리하여 개발 단계에서 빨리 드러나게
            return false;
        }
    }
}