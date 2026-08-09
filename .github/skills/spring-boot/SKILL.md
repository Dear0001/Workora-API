---
name: spring-boot
description: Use this skill when building or maintaining Spring Boot 3.x applications with REST APIs, JPA, security, testing, and cloud-native patterns.
---

# Spring Boot Skill

Use this skill for enterprise Java backend work with Spring Boot 3.x. Focus on clean architecture, production-ready APIs, and maintainable service layers.

## When to Use This Skill

Use this skill when you need to:
- create or evolve REST APIs in Spring Boot
- add or modify JPA entities, repositories, and services
- implement authentication, authorization, or JWT-based security
- add validation, exception handling, and structured API responses
- write unit or integration tests for Spring components
- align the codebase with cloud-native and production-ready patterns

## Core Workflow

1. Analyze
   - Clarify the business requirement, API contract, persistence needs, and non-functional constraints.
   - Identify service boundaries, DTOs, entities, repositories, and any external integrations.

2. Design
   - Prefer layered architecture: controller → service → repository.
   - Confirm request/response shapes, validation rules, transaction boundaries, and error handling before coding.

3. Implement
   - Use constructor injection and explicit dependencies.
   - Keep controllers thin, services focused on business logic, and repositories responsible for persistence.
   - Follow Spring Boot 3.x conventions and avoid deprecated patterns.

4. Secure
   - Add validation for request payloads.
   - Apply security rules with Spring Security 6 patterns and method-level authorization where appropriate.
   - Keep secrets out of source control and prefer environment variables or secret managers.

5. Test
   - Add unit tests for services and controller slice tests where useful.
   - Validate integration behavior with realistic scenarios.
   - Run the relevant test suite and confirm it passes before finishing.

6. Validate
   - Ensure APIs return clear error responses and behave predictably.
   - Verify configuration is externalized and production-safe.

## Quick Start Templates

### Entity

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @DecimalMin("0.0")
    private BigDecimal price;

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
```

### Repository

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
}
```

### Service

```java
@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Product create(ProductRequest request) {
        var product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        return productRepository.save(product);
    }
}
```

### REST Controller

```java
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> search(@RequestParam(defaultValue = "") String name) {
        return productService.search(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }
}
```

### DTO Record

```java
public record ProductRequest(
    @NotBlank String name,
    @DecimalMin("0.0") BigDecimal price
) {}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid"
            ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(EntityNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }
}
```

## Must Do

- use constructor injection
- validate request bodies with @Valid
- use @Transactional for multi-step writes
- use @Transactional(readOnly = true) for read-only operations
- externalize secrets and environment-specific settings
- provide consistent exception handling and meaningful API errors

## Must Not Do

- use field injection
- skip validation on write endpoints
- mix blocking and reactive code in the same code path
- hardcode URLs, credentials, or environment values
- use Spring Boot 2.x style patterns when Spring Boot 3.x is available

## Architecture Patterns

Prefer this structure:

```text
src/main/java/com/example/app/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── config/
└── exception/
```

Core layering:
- Controller handles HTTP concerns and validation
- Service handles business logic and transactions
- Repository handles persistence
- DTOs keep API contracts explicit and stable

## Security and Cloud-Native Guidance

- Prefer Spring Security 6 configuration and method security where appropriate.
- Support JWT or OAuth2/OIDC when the requirement calls for token-based authentication.
- Keep configuration externalized and environment-aware.
- Expose health and readiness information with actuator endpoints when appropriate.

## Testing Guidance

- Use unit tests for pure service logic.
- Use slice tests for controllers and web layers.
- Add integration tests for persistence and cross-component behavior when necessary.
- Run the relevant Maven test command before finalizing changes.

## Completion Checklist

Before considering the work complete, verify that:
- the API contract is clear and validated
- the code follows the chosen architecture
- the relevant tests pass
- secrets and environment values are not hardcoded
- errors are handled consistently and clearly
