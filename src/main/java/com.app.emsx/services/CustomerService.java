package com.app.emsx.services;

import com.app.emsx.dtos.customer.CustomerRequest;
import com.app.emsx.dtos.customer.CustomerResponse;
import com.app.emsx.dtos.order.OrderResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse getById(Long id);
    List<CustomerResponse> getAll();
    CustomerResponse update(Long id, CustomerRequest request);
    void delete(Long id);
    List<OrderResponse> getOrderHistory(Long customerId);
}


