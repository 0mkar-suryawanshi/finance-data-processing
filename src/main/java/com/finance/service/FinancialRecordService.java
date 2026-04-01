package com.finance.service;


import com.finance.entity.FinancialRecord;
import com.finance.entity.TransactionType;
import com.finance.entity.User;
import com.finance.exception.AccessDeniedException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.payload.request.FinancialRecordRequest;
import com.finance.payload.response.FinancialRecordResponse;
import com.finance.payload.response.SummaryResponse;
import com.finance.repository.FinancialRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinancialRecordService {
    @Autowired
    private FinancialRecordRepository recordRepository;

    @Autowired
    private UserService userService;

    public FinancialRecordResponse createRecord(FinancialRecordRequest request, User currentUser) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        record.setUser(currentUser);
        record = recordRepository.save(record);
        return mapToResponse(record);
    }

    public Page<FinancialRecordResponse> getRecordsForUser(User user, Pageable pageable) {
        return recordRepository.findByUser(user, pageable).map(this::mapToResponse);
    }

    public FinancialRecordResponse getRecordById(Long id, User currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        // Check ownership: current user must own the record OR have admin role
        if (!record.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to view this record");
        }
        return mapToResponse(record);
    }

    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request, User currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        // Ownership check
        if (!record.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to update this record");
        }
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        record = recordRepository.save(record);
        return mapToResponse(record);
    }

    @Transactional
    public void deleteRecord(Long id, User currentUser) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        if (!record.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have permission to delete this record");
        }
        recordRepository.delete(record);
    }

    public SummaryResponse getSummary(User currentUser, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) startDate = LocalDateTime.MIN;
        if (endDate == null) endDate = LocalDateTime.MAX;

        BigDecimal totalIncome = recordRepository.sumByUserAndTypeAndDateBetween(currentUser, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpenses = recordRepository.sumByUserAndTypeAndDateBetween(currentUser, TransactionType.EXPENSE, startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Category totals for expenses (or both)
        List<Object[]> expenseCategoryTotals = recordRepository.findCategoryTotals(currentUser, TransactionType.EXPENSE, startDate, endDate);
        Map<String, BigDecimal> categoryTotals = expenseCategoryTotals.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (BigDecimal) obj[1]
                ));

        // Recent activity: last 5 records
        Page<FinancialRecord> recent = recordRepository.findByUser(currentUser, Pageable.ofSize(5));
        List<FinancialRecordResponse> recentActivity = recent.stream().map(this::mapToResponse).toList();

        return new SummaryResponse(totalIncome, totalExpenses, netBalance, categoryTotals, recentActivity);
    }

    // Helper to map entity to response DTO
    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        FinancialRecordResponse response = new FinancialRecordResponse();
        response.setId(record.getId());
        response.setAmount(record.getAmount());
        response.setType(record.getType());
        response.setCategory(record.getCategory());
        response.setDate(record.getDate());
        response.setDescription(record.getDescription());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }
}
