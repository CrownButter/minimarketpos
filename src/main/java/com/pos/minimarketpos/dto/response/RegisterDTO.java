package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RegisterDTO {
    private Long id;
    private Long userId;
    private Long storeId;
    private BigDecimal cashInhand;
    private BigDecimal cashTotal;
    private BigDecimal cashSub;
    private BigDecimal ccTotal;
    private BigDecimal ccSub;
    private BigDecimal chequeTotal;
    private BigDecimal chequeSub;
    private String note;
    private Long closedBy;
    private LocalDateTime closedAt;
    private LocalDateTime date;
    private Integer status;
}
