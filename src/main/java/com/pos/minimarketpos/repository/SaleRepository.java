package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // --- 1. METHOD LAMA (YANG HILANG & MENYEBABKAN ERROR) ---

    List<Sale> findByRegisterId(Long registerId);
    List<Sale> findByClientId(Long clientId);
    List<Sale> findByStatus(Integer status);

    // Query untuk ReportService (Date Range)
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    List<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Query untuk ReportService (Today Total)
    // Menggunakan CURRENT_DATE agar sesuai logic lama
    @Query("SELECT SUM(s.total) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    BigDecimal getTodayTotal();

    // Query untuk ReportService (Today Profit)
    // Asumsi Profit = Total - Cost
    @Query("SELECT SUM(s.total - s.cost) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    BigDecimal getTodayProfit();


    // --- 2. METHOD BARU (OPTIMASI STATISTIK TOKO) ---

    // Total Sales per Toko
    @Query("SELECT SUM(s.total) FROM Sale s JOIN s.register r WHERE r.store.id = :storeId")
    BigDecimal sumTotalSalesByStoreId(@Param("storeId") Long storeId);

    // Monthly/Daily Sales per Toko
    @Query("SELECT SUM(s.total) FROM Sale s JOIN s.register r WHERE r.store.id = :storeId AND s.createdAt BETWEEN :start AND :end")
    BigDecimal sumSalesByStoreIdAndDateRange(@Param("storeId") Long storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Count Transaksi per Toko
    @Query("SELECT COUNT(s) FROM Sale s JOIN s.register r WHERE r.store.id = :storeId")
    Long countTransactionsByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(s) FROM Sale s JOIN s.register r WHERE r.store.id = :storeId AND s.createdAt BETWEEN :start AND :end")
    Long countTransactionsByStoreIdAndDateRange(@Param("storeId") Long storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Pagination support (Optional tapi bagus)
    Page<Sale> findByRegisterId(Long registerId, Pageable pageable);
}