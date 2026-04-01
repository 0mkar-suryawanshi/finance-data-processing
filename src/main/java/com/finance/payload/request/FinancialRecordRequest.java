package com.finance.payload.request;



import com.finance.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialRecordRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    @NotNull
    private String category;

    @NotNull
    private LocalDateTime date;

    private String description;

    // getters and setters
}
