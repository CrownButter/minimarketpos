package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);
    List<SaleItem> findByProductId(Long productId);

    @Query("SELECT si FROM SaleItem si WHERE " +
            "si.productId = :productId AND si.date BETWEEN :start AND :end")
    List<SaleItem> findByProductAndDateRange(
            @Param("productId") Long productId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT si.name, si.productId, SUM(si.qt) as totalQuantity " +
            "FROM SaleItem si WHERE YEAR(si.date) = :year " +
            "GROUP BY si.productId, si.name " +
            "ORDER BY SUM(si.qt) DESC")
    List<Object[]> findTop5Products(@Param("year") int year);
}
