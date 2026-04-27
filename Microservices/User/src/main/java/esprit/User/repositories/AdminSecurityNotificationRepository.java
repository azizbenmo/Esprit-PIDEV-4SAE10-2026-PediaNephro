package esprit.User.repositories;

import esprit.User.entities.AdminSecurityNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdminSecurityNotificationRepository extends JpaRepository<AdminSecurityNotification, Long> {

    List<AdminSecurityNotification> findTop50ByOrderByCreatedAtDesc();

    List<AdminSecurityNotification> findTop50BySeenByAdminFalseOrderByCreatedAtDesc();

    long countBySeenByAdminFalse();

    @Modifying
    @Transactional
    @Query("update AdminSecurityNotification n set n.seenByAdmin = true where n.seenByAdmin = false")
    int markAllUnreadAsSeen();
}
