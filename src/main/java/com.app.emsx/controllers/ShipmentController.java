package com.app.emsx.controllers;

import com.app.emsx.common.ApiResponse;
import com.app.emsx.dtos.shipment.ShipmentRequest;
import com.app.emsx.dtos.shipment.ShipmentResponse;
import com.app.emsx.services.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(@Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse response = shipmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Shipment created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getById(@PathVariable Long id) {
        ShipmentResponse response = shipmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Shipment retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getAll() {
        List<ShipmentResponse> responses = shipmentService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("Shipments retrieved successfully", responses));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ShipmentResponse response = shipmentService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Shipment status updated successfully", response));
    }

    @GetMapping("/track/order/{orderId}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> trackByOrderId(@PathVariable Long orderId) {
        ShipmentResponse response = shipmentService.trackByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment tracking retrieved successfully", response));
    }

    @GetMapping("/track/code/{trackingCode}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> trackByTrackingCode(@PathVariable String trackingCode) {
        ShipmentResponse response = shipmentService.trackByTrackingCode(trackingCode);
        return ResponseEntity.ok(ApiResponse.ok("Shipment tracking retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Shipment deleted successfully", null));
    }
}

