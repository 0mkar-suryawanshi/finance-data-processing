package com.finance.repository;


import com.finance.entity.FinancialRecord;
import com.finance.entity.TransactionType;
import com.finance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {
    Page<FinancialRecord> findByUser(User user, Pageable pageable);
    Page<FinancialRecord> findByUserAndType(User user, TransactionType type, Pageable pageable);
    Page<FinancialRecord> findByUserAndCategory(User user, String category, Pageable pageable);
    Page<FinancialRecord> findByUserAndDateBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user = :user AND f.type = :type AND f.date BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("user") User user,
                                              @Param("type") TransactionType type,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT f.category, COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user = :user AND f.type = :type AND f.date BETWEEN :start AND :end GROUP BY f.category")
    List<Object[]> findCategoryTotals(@Param("user") User user,
                                      @Param("type") TransactionType type,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}
