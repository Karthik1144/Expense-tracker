package com.expensetracker.controller;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        return ResponseEntity.ok(transactionService.list(
                principal.getId(), categoryId, type, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(principal.getId(), id));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        transactionService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
