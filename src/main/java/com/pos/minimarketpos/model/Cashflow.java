package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashflows", indexes = {
        @Index(name = "idx_cashflow_date", columnList = "date"),
        @Index(name = "idx_cashflow_store", columnList = "store_id"),
        @Index(name = "idx_cashflow_category", columnList = "category_id"),
        @Index(name = "idx_cashflow_created_by", columnList = "created_by"),
        @Index(name = "idx_cashflow_flow_type", columnList = "flow_type"),
        @Index(name = "idx_cashflow_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Cashflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryCashflow category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "lunas", nullable = false)
    @Builder.Default
    private Boolean lunas = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 500)
    private String attachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false, length = 20)
    private CashflowType flowType;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    // ========== HELPER METHODS ==========

    /**
     * Check if this is an inflow transaction
     */
    public boolean isInflow() {
        return CashflowType.INFLOW.equals(this.flowType);
    }

    /**
     * Check if this is an outflow transaction
     */
    public boolean isOutflow() {
        return CashflowType.OUTFLOW.equals(this.flowType);
    }

    /**
     * Get amount with sign (negative for outflow, positive for inflow)
     */
    public BigDecimal getSignedAmount() {
        return isOutflow() ? amount.negate() : amount;
    }

    /**
     * Soft delete this cashflow
     */
    public void softDelete(User user) {
        this.deleted = true;
        this.deletedDate = LocalDateTime.now();
        this.deletedBy = user;
    }

    /**
     * Restore soft deleted cashflow
     */
    public void restore() {
        this.deleted = false;
        this.deletedDate = null;
        this.deletedBy = null;
    }

    // ========== CASHFLOW TYPE ENUM ==========

    /**
     * Enum for cashflow type (Inflow/Outflow)
     */
    public enum CashflowType {
        INFLOW("Pemasukan", "Income"),
        OUTFLOW("Pengeluaran", "Expense");

        private final String labelId;
        private final String labelEn;

        CashflowType(String labelId, String labelEn) {
            this.labelId = labelId;
            this.labelEn = labelEn;
        }

        public String getLabelId() {
            return labelId;
        }

        public String getLabelEn() {
            return labelEn;
        }

        public String getLabel() {
            return labelId; // Default to Indonesian
        }
    }
}