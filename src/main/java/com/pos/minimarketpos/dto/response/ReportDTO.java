package com.pos.minimarketpos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private BigDecimal todaySales;
    private BigDecimal todayProfit;
    private BigDecimal todayCost;
    private Integer todayOrders;
    private Map<String, BigDecimal> monthlySales;
    private Map<String, BigDecimal> monthlyExpenses;
    private List<ProductSales> topProducts;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSales {
        private Long productId;
        private String productName;
        private Integer totalQuantity;
        private BigDecimal totalAmount;
    }
}
