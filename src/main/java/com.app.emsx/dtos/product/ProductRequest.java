package com.app.emsx.dtos.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 7, fraction = 2, message = "Price must not exceed 7 digits")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    @Size(max = 15, message = "Category must be at most 15 characters")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ]+$", message = "Category must contain only letters")
    private String category;

    @NotBlank(message = "SKU is required")
    @Size(max = 12, message = "SKU must be at most 12 characters")
    private String sku;

    @NotNull(message = "Initial stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    @Max(value = 99999, message = "Stock quantity must be at most 5 digits")
    private Integer initialStock;
}


