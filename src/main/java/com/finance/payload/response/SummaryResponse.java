package com.finance.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> categoryTotals;
    private List<FinancialRecordResponse> recentActivity;
    // optionally monthly trends: Map<String, BigDecimal> monthlyIncome, monthlyExpenses
}