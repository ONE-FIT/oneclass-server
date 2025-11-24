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

/**
 * MemberRepository
 * - 기본 단건/일괄 조회
 * - 출석/수업 관련 편의 쿼리
 * - 교사-학생/부모-학생 연관 fetch 쿼리
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m.phone FROM Member m WHERE m.lesson.lessonId = :lessonId AND m.phone IS NOT NULL AND m.phone != ''")
    Page<String> findPhonesByLessonId(@Param("lessonId") Long lessonId, Pageable pageable);

    // 단건 조회
    Optional<Member> findByUsername(String username);
    Optional<Member> findByPhone(String phone);


    // 다건 조회(전화번호 리스트로)
    List<Member> findAllByPhoneIn(Collection<String> phones);

    @Query("select m.phone from Member m where m.role in (oneclass.oneclass.domain.member.entity.Role.STUDENT, oneclass.oneclass.domain.member.entity.Role.PARENT)")
    Page<String> findAllPhones(Pageable pageable);

    // 특정 학생 id 집합으로 전화번호 페이징
    @Query("select m.phone from Member m where m.id in :studentIds")
    Page<String> findPhonesByIds(@Param("studentIds") List<Long> studentIds, Pageable pageable);

    /**
     * 수업별 & 날짜별 '결석' 대상 학생 목록
     * 조건: 해당 lesson 의 students 중 그 날짜에 PRESENT/LATE/EXCUSED 기록이 없는 사람
     */
    @Query("""
        select m
        from Member m
        where m in (
            select s from Lesson l
            join l.students s
            where l.lessonId = :lessonId
        )
        and not exists (
            select 1 from Attendance a
            where a.member.id = m.id
              and a.date = :date
              and a.attendanceStatus in ('PRESENT','LATE','EXCUSED')
        )
    """)
    List<Member> findAbsentMembersByLessonAndDate(@Param("lessonId") Long lessonId,
                                                  @Param("date") LocalDate date);

    /**
     * (선택) 날짜만으로 결석자 전체 조회가 필요하다면 아래 메서드 사용.
     * 출석 인정 상태(PRESENT/LATE/EXCUSED)가 그 날짜에 하나도 없는 사용자.
     * 필요 없으면 제거.
     */
    @Query("""
        select m
        from Member m
        where not exists (
            select 1 from Attendance a
            where a.member.id = m.id
              and a.date = :date
              and a.attendanceStatus in ('PRESENT','LATE','EXCUSED')
        )
    """)
    List<Member> findAbsentMembers(@Param("date") LocalDate date);

    /**
     * 교사 → 담당 학생 목록 fetch (teachingStudents)
     * 컬렉션 한 개만 fetch 하므로 안전.
     */
    @EntityGraph(attributePaths = {"teachingStudents"})
    Optional<Member> findWithTeachingStudentsByPhone(String phone);

    /**
     * 학생 → (teachers, parents) 모두 필요할 때
     * 다중 컬렉션 fetch join은 row 곱셈 가능. 데이터 양 많으면 비용 커질 수 있음.
     * Set 기반 매핑이라면 중복 병합은 되지만 성능 고려 필요.
     */
    @EntityGraph(attributePaths = {"teachers", "parents"})
    Optional<Member> findWithTeachersAndParentsByPhone(String phone);

    /**
     * fetch join 버전 (distinct)
     * teachers + parents 한 번에 로드. 위 EntityGraph와 중복 용도이므로 둘 중 하나만 유지 권장.
     */
    @Query("""
        select distinct m
        from Member m
        left join fetch m.teachers
        left join fetch m.parents
        where m.phone = :phone
    """)
    Optional<Member> findStudentWithTeachersAndParentsByPhoneFetchJoin(@Param("phone") String phone);


    List<Member> findAllByUsernameIn(Collection<String> usernames);

}