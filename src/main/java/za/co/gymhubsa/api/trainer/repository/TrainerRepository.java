package za.co.gymhubsa.api.trainer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserId(Long userId);

    Optional<Trainer> findByUser_EmailIgnoreCase(String email);

    boolean existsByUserId(Long userId);

    @Query("""
            select t from Trainer t
            join fetch t.user u
            where (:keyword is null
                    or lower(u.firstName) like lower(concat('%', :keyword, '%'))
                    or lower(u.lastName) like lower(concat('%', :keyword, '%'))
                    or lower(t.specialization) like lower(concat('%', :keyword, '%')))
              and (:status is null or t.status = :status)
            """)
    Page<Trainer> search(@Param("keyword") String keyword, @Param("status") TrainerStatus status, Pageable pageable);
}
