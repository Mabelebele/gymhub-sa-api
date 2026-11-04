package za.co.gymhubsa.api.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
