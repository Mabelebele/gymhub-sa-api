package za.co.gymhubsa.api.membership.dto;

import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MemberMembershipResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Long planId,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal pricePaid,
        MembershipStatus status
) {
    public static MemberMembershipResponse from(MemberMembership membership) {
        return new MemberMembershipResponse(
                membership.getId(),
                membership.getMember().getId(),
                membership.getMember().getUser().getFirstName() + " " + membership.getMember().getUser().getLastName(),
                membership.getMembershipPlan().getId(),
                membership.getMembershipPlan().getName(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getPricePaid(),
                membership.getStatus()
        );
    }
}
