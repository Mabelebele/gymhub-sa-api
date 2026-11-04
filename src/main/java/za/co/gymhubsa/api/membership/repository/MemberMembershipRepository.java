package za.co.gymhubsa.api.membership.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberMembershipRepository extends JpaRepository<MemberMembership, Long> {

    Optional<MemberMembership> findFirstByMemberIdAndStatusOrderByStartDateDesc(Long memberId, MembershipStatus status);

    Page<MemberMembership> findByMemberIdOrderByStartDateDesc(Long memberId, Pageable pageable);

    List<MemberMembership> findByStatusAndEndDateBetween(MembershipStatus status, LocalDate from, LocalDate to);

    List<MemberMembership> findByStatusAndEndDateBefore(MembershipStatus status, LocalDate date);

    List<MemberMembership> findByStatus(MembershipStatus status);

    long countByStatus(MembershipStatus status);
}
