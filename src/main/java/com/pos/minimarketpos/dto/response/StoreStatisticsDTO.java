package com.pos.minimarketpos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreStatisticsDTO {
    private Long storeId;
    private String storeName;
    private Long totalRegisters;
    private Long openRegisters;
    private BigDecimal totalSales;
    private BigDecimal monthlySales;
    private Long totalTransactions;
    private Long monthlyTransactions;
    private BigDecimal averageTransactionValue;
    private Boolean isActive;

    public String getStatisticsSummary() {
        return String.format("Store: %s | Monthly Sales: %s | Trx: %d",
                storeName,
                monthlySales != null ? monthlySales.toString() : "0.00",
                monthlyTransactions != null ? monthlyTransactions : 0);
    }
}