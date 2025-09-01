package oneclass.oneclass.domain.student.attendance.assignment;

import jakarta.persistence.*;
import oneclass.oneclass.domain.student.attendance.entity.BaseTimeEntity;

import java.time.LocalDate;

// 실제 "과제" 엔티티
@Entity
public class Assignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // 과제 제목
    private String description;    // 과제 설명
    private LocalDate dueDate;     // 마감일

    // 연관관계 (예: 어떤 학생/수업의 과제인지)
//    @ManyToOne
//    private Student student;
//
//    @ManyToOne
//    private Course course;

    // getter/setter ...
}