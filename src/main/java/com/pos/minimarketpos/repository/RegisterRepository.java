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

    // Ubah parameter nama field dari storeId menjadi store.id
    // JPA cukup pintar, jika Anda pakai findByStoreId, dia akan mencari field store lalu id-nya

    List<Register> findByStoreId(Long storeId); // Ini tetap VALID (JPA Magic)

    Optional<Register> findByStoreIdAndStatus(Long storeId, Integer status); // Ini tetap VALID

    List<Register> findByUserId(Long userId);

    // --- Query Manual & Count (Optimized) ---

    // Gunakan 'store.id' bukan 'storeId'
    @Query("SELECT COUNT(r) FROM Register r WHERE r.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(r) FROM Register r WHERE r.store.id = :storeId AND r.status = :status")
    long countByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") Integer status);

    // Query Range Tanggal
    @Query("SELECT r FROM Register r WHERE " +
            "r.store.id = :storeId AND r.date BETWEEN :start AND :end")
    List<Register> findByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}