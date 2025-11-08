package oneclass.oneclass.domain.member.repository;

import oneclass.oneclass.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByPhone(String phone);



    @Query("select m.phone from Member m")
    Page<String> findAllPhones(Pageable pageable);

    @Query("""
    SELECT m FROM Member m
    WHERE m IN (
        SELECT s FROM Lesson l
        JOIN l.students s
        WHERE l.lessonId = :lessonId
    )
    AND NOT EXISTS (
        SELECT 1 FROM Attendance a
        WHERE a.member.id = m.id
          AND a.date = :date
          AND a.attendanceStatus IN ('PRESENT', 'LATE', 'EXCUSED')
    )
""")
    List<Member> findAbsentMembersByLessonAndDate(@Param("lessonId") Long lessonId,
                                                  @Param("date") LocalDate date);
    """)
    List<Member> findAbsentMembers(LocalDate date);


    boolean existsByPhone(String phone);
    boolean existsByUsername(String username);

    @Query("SELECT m.phone FROM Member m WHERE m.id IN :studentIds")
    Page<String> findPhonesByIds(@Param("studentIds") List<Long> studentIds, Pageable pageable);


    // 배치 조회
    List<Member> findAllByPhoneIn(Collection<String> phones);

    @EntityGraph(attributePaths = {"teachingStudents"})
    Optional<Member> findWithTeachingStudentsByPhone(String phone);

    @EntityGraph(attributePaths = {"teachers", "parents"})
    Optional<Member> findWithTeachersAndParentsByPhone(String phone);

    @Query("select distinct m from Member m " +
            "left join fetch m.teachers " +
            "left join fetch m.parents " +
            "where m.phone = :phone")
    Optional<Member> findStudentWithTeachersAndParentsByPhoneFetchJoin(String phone);
}




