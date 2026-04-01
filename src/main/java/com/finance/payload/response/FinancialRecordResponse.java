package com.finance.payload.response;



import com.finance.entity.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialRecordResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDateTime date;
    private String description;
    private LocalDateTime createdAt;

    // constructor, getters, setters
}
