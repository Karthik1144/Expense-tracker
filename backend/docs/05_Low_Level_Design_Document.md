# Low-Level Design (LLD) Document
## Expense Tracker Application

**Version:** 1.0

---

## 1. Package Structure

```
com.karthik.expensetracker
├── config
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── CorsConfig.java
├── security
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── controller
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── TransactionController.java
│   ├── BudgetController.java
│   └── DashboardController.java
├── service
│   ├── AuthService.java
│   ├── CategoryService.java
│   ├── TransactionService.java
│   ├── BudgetService.java
│   └── AnalyticsService.java
├── repository
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── TransactionRepository.java
│   └── BudgetRepository.java
├── entity
│   ├── User.java
│   ├── Category.java
│   ├── Transaction.java
│   └── Budget.java
├── dto
│   ├── request/  (RegisterRequest, LoginRequest, TransactionRequest, BudgetRequest...)
│   └── response/ (AuthResponse, TransactionResponse, DashboardSummaryResponse...)
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
└── ExpenseTrackerApplication.java
```

---

## 2. Key Class Designs

### 2.1 Entity: `Transaction`
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // INCOME, EXPENSE

    private String description;
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2.2 Repository: `TransactionRepository`
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdAndTransactionDateBetween(
        Long userId, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT c.name, SUM(t.amount) FROM Transaction t JOIN t.category c " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND FUNCTION('YEAR', t.transactionDate) = :year " +
           "AND FUNCTION('MONTH', t.transactionDate) = :month " +
           "GROUP BY c.name")
    List<Object[]> getCategoryBreakdown(Long userId, int year, int month);
}
```

### 2.3 Service: `TransactionService`
```java
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionResponse create(Long userId, TransactionRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction txn = Transaction.builder()
            .user(userRef(userId))
            .category(category)
            .amount(req.getAmount())
            .type(req.getType())
            .description(req.getDescription())
            .paymentMethod(req.getPaymentMethod())
            .transactionDate(req.getTransactionDate())
            .build();

        return TransactionMapper.toResponse(transactionRepository.save(txn));
    }

    public void deleteOwnedTransaction(Long userId, Long txnId) {
        Transaction txn = transactionRepository.findById(txnId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!txn.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not your transaction");
        }
        transactionRepository.delete(txn);
    }
}
```

### 2.4 Security: `JwtAuthenticationFilter`
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {
        String token = extractToken(req);
        if (token != null && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
```

### 2.5 Analytics: discretionary-spending reduction calculation
```java
public DiscretionaryInsightResponse getDiscretionaryInsight(Long userId, YearMonth current) {
    YearMonth previous = current.minusMonths(1);
    BigDecimal currentSpend = transactionRepository.sumDiscretionary(userId, current);
    BigDecimal previousSpend = transactionRepository.sumDiscretionary(userId, previous);

    BigDecimal pctChange = previousSpend.compareTo(BigDecimal.ZERO) == 0
        ? BigDecimal.ZERO
        : currentSpend.subtract(previousSpend)
            .divide(previousSpend, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

    return new DiscretionaryInsightResponse(currentSpend, previousSpend, pctChange);
}
```

---

## 3. Sequence: Login Flow

```
Client → AuthController.login(LoginRequest)
       → AuthService.authenticate(email, password)
             → AuthenticationManager.authenticate(...)
             → JwtTokenProvider.generateToken(user)
       ← AuthResponse(accessToken, expiresIn)
Client stores token → attaches to subsequent requests
```

---

## 4. Validation Rules (DTO-level, via Bean Validation)

| Field | Rule |
|---|---|
| email | `@Email @NotBlank` |
| password | `@Size(min=8)` + pattern (upper/lower/digit) |
| amount | `@DecimalMin("0.01")` |
| transactionDate | `@NotNull @PastOrPresent` |
| categoryId | `@NotNull` |

---

## 5. Design Patterns Used
- **DTO Pattern** — decouples entities from API contracts.
- **Builder Pattern** — entity/DTO construction (Lombok `@Builder`).
- **Strategy-like filter chain** — Spring Security filters.
- **Repository Pattern** — Spring Data JPA abstraction.
- **Global Exception Handler** — centralized error mapping (`@ControllerAdvice`).
