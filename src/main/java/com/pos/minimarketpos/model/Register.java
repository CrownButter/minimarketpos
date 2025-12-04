package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- PERUBAHAN PENTING DI SINI ---

    // Ganti Long userId menjadi User user (Asumsi Anda punya User entity)
    // Jika User entity belum siap, sementara biarkan Long userId,
    // tapi Store WAJIB diubah untuk fitur statistik.

    @Column(name = "user_id")
    private Long userId;
    // Idealnya:
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id")
    // private User user;

    // Ganti Long storeId menjadi Store store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // Ini akan tetap membuat kolom 'store_id' di DB
    private Store store;

    // --- BATAS PERUBAHAN ---

    @Column(name = "cash_inhand", precision = 10, scale = 2)
    private BigDecimal cashInhand;

    @Column(name = "cash_total", precision = 10, scale = 2)
    private BigDecimal cashTotal;

    @Column(name = "cash_sub", precision = 10, scale = 2)
    private BigDecimal cashSub;

    @Column(name = "cc_total", precision = 10, scale = 2)
    private BigDecimal ccTotal;

    @Column(name = "cc_sub", precision = 10, scale = 2)
    private BigDecimal ccSub;

    @Column(name = "cheque_total", precision = 10, scale = 2)
    private BigDecimal chequeTotal;

    @Column(name = "cheque_sub", precision = 10, scale = 2)
    private BigDecimal chequeSub;

    private String note;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    private LocalDateTime date;
    private Integer status;
}