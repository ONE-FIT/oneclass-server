package oneclass.oneclass.domain.auth.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import oneclass.oneclass.domain.task.entity.TaskAssignment;

import java.util.List;

@Entity
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 이름
    private String password; // 비번

    private String phone;
    private String email;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignment> taskAssignments;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT / TEACHER / PARENT
}

