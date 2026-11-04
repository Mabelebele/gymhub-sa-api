package za.co.gymhubsa.api.member.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("""
            select m from Member m
            join fetch m.user u
            where (:keyword is null
                    or lower(u.firstName) like lower(concat('%', :keyword, '%'))
                    or lower(u.lastName) like lower(concat('%', :keyword, '%'))
                    or lower(u.email) like lower(concat('%', :keyword, '%')))
              and (:status is null or m.status = :status)
            """)
    Page<Member> search(@Param("keyword") String keyword, @Param("status") MemberStatus status, Pageable pageable);
}
