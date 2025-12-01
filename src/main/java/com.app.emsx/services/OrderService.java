package com.app.emsx.services;

import com.app.emsx.dtos.order.OrderRequest;
import com.app.emsx.dtos.order.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse create(OrderRequest request);
    OrderResponse getById(Long id);
    List<OrderResponse> getAll();
    OrderResponse addOrderItems(Long orderId, List<com.app.emsx.dtos.order.OrderItemRequest> items);
    OrderResponse updateStatus(Long id, String status);
    void delete(Long id);
}


