package com.meze.repository;

import com.meze.domains.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken,Long>{
    Optional<PasswordResetToken> findByToken(String token);
    @Transactional
    @Modifying
    @Query("UPDATE PasswordResetToken p " +
            "SET p.usedAt = :confirmedAt " +
            "WHERE p.token = :token")
    void updateConfirmedAt(@Param("token")String token,
                           @Param("confirmedAt") LocalDateTime confirmedAt);
}
