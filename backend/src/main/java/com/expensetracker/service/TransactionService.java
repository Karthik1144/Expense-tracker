package com.expensetracker.service;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.exception.ForbiddenOperationException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.TransactionSpecifications;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = categoryRepository.findByIdAccessibleByUser(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .transactionDate(request.getTransactionDate())
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = getOwnedOrThrow(userId, transactionId);

        Category category = categoryRepository.findByIdAccessibleByUser(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTransactionDate(request.getTransactionDate());

        return toResponse(transactionRepository.save(transaction));
    }

    public TransactionResponse getById(Long userId, Long transactionId) {
        return toResponse(getOwnedOrThrow(userId, transactionId));
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Transaction transaction = getOwnedOrThrow(userId, transactionId);
        transactionRepository.delete(transaction);
    }

    public Page<TransactionResponse> list(Long userId, Long categoryId, TransactionType type,
                                           LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.belongsToUser(userId))
                .and(TransactionSpecifications.hasCategory(categoryId))
                .and(TransactionSpecifications.hasType(type))
                .and(TransactionSpecifications.dateBetween(startDate, endDate));

        return transactionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private Transaction getOwnedOrThrow(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("You do not have access to this transaction");
        }
        return transaction;
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .paymentMethod(t.getPaymentMethod())
                .transactionDate(t.getTransactionDate())
                .build();
    }
}
