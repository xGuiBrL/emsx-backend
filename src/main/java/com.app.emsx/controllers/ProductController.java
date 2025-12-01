package com.app.emsx.controllers;

import com.app.emsx.common.ApiResponse;
import com.app.emsx.dtos.product.ProductRequest;
import com.app.emsx.dtos.product.ProductResponse;
import com.app.emsx.dtos.stock.StockResponse;
import com.app.emsx.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse response = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll() {
        List<ProductResponse> responses = productService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", responses));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable String category) {
        List<ProductResponse> responses = productService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", responses));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailableProducts() {
        List<ProductResponse> responses = productService.getAvailableProducts();
        return ResponseEntity.ok(ApiResponse.ok("Available products retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto dado de baja correctamente", null));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<StockResponse>> getStock(@PathVariable Long id) {
        StockResponse response = productService.getStock(id);
        return ResponseEntity.ok(ApiResponse.ok("Stock retrieved successfully", response));
    }
}

