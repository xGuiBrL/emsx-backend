package com.app.emsx.controllers;

import com.app.emsx.common.ApiResponse;
import com.app.emsx.dtos.order.OrderItemRequest;
import com.app.emsx.dtos.order.OrderRequest;
import com.app.emsx.dtos.order.OrderResponse;
import com.app.emsx.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        OrderResponse response = orderService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll() {
        List<OrderResponse> responses = orderService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved successfully", responses));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderResponse>> addOrderItems(
            @PathVariable Long id,
            @Valid @RequestBody List<OrderItemRequest> items) {
        OrderResponse response = orderService.addOrderItems(id, items);
        return ResponseEntity.ok(ApiResponse.ok("Order items added successfully", response));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderResponse response = orderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Order deleted successfully", null));
    }
}

