package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {
    Optional<Register> findByStoreIdAndStatus(Long storeId, Integer status);
    List<Register> findByUserId(Long userId);
    List<Register> findByStoreId(Long storeId);

    @Query("SELECT r FROM Register r WHERE " +
            "r.storeId = :storeId AND r.date BETWEEN :start AND :end")
    List<Register> findByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
