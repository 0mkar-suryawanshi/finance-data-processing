package com.finance.controller;


import com.finance.entity.User;
import com.finance.payload.request.FinancialRecordRequest;
import com.finance.payload.response.FinancialRecordResponse;
import com.finance.payload.response.SummaryResponse;
import com.finance.security.UserDetailsImpl;
import com.finance.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {
    @Autowired
    private FinancialRecordService recordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Page<FinancialRecordResponse>> getRecords(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable) {
        User user = getUserFromDetails(currentUser);
        return ResponseEntity.ok(recordService.getRecordsForUser(user, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<FinancialRecordResponse> getRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = getUserFromDetails(currentUser);
        return ResponseEntity.ok(recordService.getRecordById(id, user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinancialRecordResponse> createRecord(
            @Valid @RequestBody FinancialRecordRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = getUserFromDetails(currentUser);
        return ResponseEntity.ok(recordService.createRecord(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = getUserFromDetails(currentUser);
        return ResponseEntity.ok(recordService.updateRecord(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<?> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = getUserFromDetails(currentUser);
        recordService.deleteRecord(id, user);
        return ResponseEntity.ok("Record deleted successfully");
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = getUserFromDetails(currentUser);
        return ResponseEntity.ok(recordService.getSummary(user, startDate, endDate));
    }

    // Helper to convert UserDetailsImpl to User entity
    private User getUserFromDetails(UserDetailsImpl userDetails) {
        User user = new User();
        user.setId(userDetails.getId());
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        // roles are already in the authorities, but we don't need them here
        return user;
    }
}
