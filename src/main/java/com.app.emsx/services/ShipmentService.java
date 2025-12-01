package com.app.emsx.services;

import com.app.emsx.dtos.shipment.ShipmentRequest;
import com.app.emsx.dtos.shipment.ShipmentResponse;

import java.util.List;

public interface ShipmentService {
    ShipmentResponse create(ShipmentRequest request);
    ShipmentResponse getById(Long id);
    List<ShipmentResponse> getAll();
    ShipmentResponse updateStatus(Long id, String status);
    ShipmentResponse trackByOrderId(Long orderId);
    ShipmentResponse trackByTrackingCode(String trackingCode);
    void delete(Long id);
}


