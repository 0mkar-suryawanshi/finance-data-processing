package com.finance.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_records")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FinancialRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INCOME or EXPENSE

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDateTime date;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;
}