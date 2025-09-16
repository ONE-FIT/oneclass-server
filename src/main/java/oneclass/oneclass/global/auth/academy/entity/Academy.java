package oneclass.oneclass.global.auth.academy.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Academy {
    @Id
    private String academyCode;
    private String academyName;
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;
}
