package za.co.gymhubsa.api.membership.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;

/**
 * Daily job that flips ACTIVE memberships to EXPIRED once their end date has
 * passed, so dashboard/reporting/notification modules can rely on status
 * being accurate without each having to re-check dates themselves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final MemberMembershipService memberMembershipService;

    @Scheduled(cron = "0 5 0 * * *")
    public void expireOverdueMemberships() {
        int expiredCount = memberMembershipService.expireOverdueMemberships();
        if (expiredCount > 0) {
            log.info("Expired {} overdue membership(s)", expiredCount);
        }
    }
}
