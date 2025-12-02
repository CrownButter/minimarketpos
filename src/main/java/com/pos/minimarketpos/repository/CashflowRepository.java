package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Cashflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashflowRepository extends JpaRepository<Cashflow, Long> {

    /**
     * Find cashflows by store ID
     */
    @Query("SELECT c FROM Cashflow c WHERE c.store.id = :storeId AND c.deleted = false")
    List<Cashflow> findByStoreId(@Param("storeId") Long storeId);

    /**
     * Find cashflows by category ID
     */
    @Query("SELECT c FROM Cashflow c WHERE c.category.id = :categoryId AND c.deleted = false")
    List<Cashflow> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find cashflows by date range
     */
    @Query("SELECT c FROM Cashflow c WHERE c.date BETWEEN :start AND :end AND c.deleted = false")
    List<Cashflow> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Find cashflows by store and date range
     */
    @Query("SELECT c FROM Cashflow c WHERE c.store.id = :storeId AND c.date BETWEEN :start AND :end AND c.deleted = false")
    List<Cashflow> findByStoreAndDateRange(@Param("storeId") Long storeId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    /**
     * Find cashflows by category and date range
     */
    @Query("SELECT c FROM Cashflow c WHERE c.category.id = :categoryId AND c.date BETWEEN :start AND :end AND c.deleted = false")
    List<Cashflow> findByCategoryAndDateRange(@Param("categoryId") Long categoryId,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    /**
     * Find cashflows by flow type
     */
    @Query("SELECT c FROM Cashflow c WHERE c.flowType = :flowType AND c.deleted = false")
    List<Cashflow> findByFlowType(@Param("flowType") Cashflow.CashflowType flowType);

    /**
     * Find cashflows by flow type and date range
     */
    @Query("SELECT c FROM Cashflow c WHERE c.flowType = :flowType AND c.date BETWEEN :start AND :end AND c.deleted = false")
    List<Cashflow> findByFlowTypeAndDateRange(@Param("flowType") Cashflow.CashflowType flowType,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    /**
     * Find cashflows by lunas status
     */
    @Query("SELECT c FROM Cashflow c WHERE c.lunas = :lunas AND c.deleted = false")
    List<Cashflow> findByLunas(@Param("lunas") Boolean lunas);

    /**
     * Find cashflows by created by user
     */
    @Query("SELECT c FROM Cashflow c WHERE c.createdBy.id = :userId AND c.deleted = false")
    List<Cashflow> findByCreatedBy(@Param("userId") Long userId);

    /**
     * Find all including deleted (for admin purposes)
     */
    @Query("SELECT c FROM Cashflow c")
    List<Cashflow> findAllIncludingDeleted();

    /**
     * Find only deleted cashflows
     */
    @Query("SELECT c FROM Cashflow c WHERE c.deleted = true")
    List<Cashflow> findDeleted();

    /**
     * Count cashflows by store
     */
    @Query("SELECT COUNT(c) FROM Cashflow c WHERE c.store.id = :storeId AND c.deleted = false")
    Long countByStoreId(@Param("storeId") Long storeId);

    /**
     * Count cashflows by category
     */
    @Query("SELECT COUNT(c) FROM Cashflow c WHERE c.category.id = :categoryId AND c.deleted = false")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Check if cashflow exists by reference
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cashflow c WHERE c.reference = :reference AND c.deleted = false")
    boolean existsByReference(@Param("reference") String reference);
}