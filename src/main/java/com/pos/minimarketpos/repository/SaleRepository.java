package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByRegisterId(Long registerId);
    List<Sale> findByClientId(Long clientId);
    List<Sale> findByCreatedBy(String createdBy);
    List<Sale> findByStatus(Integer status);

    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    List<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(s.total) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    BigDecimal getTodayTotal();

    @Query("SELECT SUM(s.total) FROM Sale s WHERE " +
            "DATE(s.createdAt) = CURRENT_DATE AND s.status = 0")
    BigDecimal getTodayTotalPaid();

    @Query("SELECT SUM(s.total - s.cost) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    BigDecimal getTodayProfit();
}
