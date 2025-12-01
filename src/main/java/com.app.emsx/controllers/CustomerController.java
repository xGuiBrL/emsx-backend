package com.app.emsx.controllers;

import com.app.emsx.common.ApiResponse;
import com.app.emsx.dtos.customer.CustomerRequest;
import com.app.emsx.dtos.customer.CustomerResponse;
import com.app.emsx.dtos.order.OrderResponse;
import com.app.emsx.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        CustomerResponse response = customerService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        List<CustomerResponse> responses = customerService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deleted successfully", null));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(@PathVariable Long id) {
        List<OrderResponse> responses = customerService.getOrderHistory(id);
        return ResponseEntity.ok(ApiResponse.ok("Order history retrieved successfully", responses));
    }
}

