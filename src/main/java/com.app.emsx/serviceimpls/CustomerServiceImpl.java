package com.app.emsx.serviceimpls;

import com.app.emsx.dtos.customer.CustomerRequest;
import com.app.emsx.dtos.customer.CustomerResponse;
import com.app.emsx.dtos.order.OrderResponse;
import com.app.emsx.entities.Customer;
import com.app.emsx.entities.Order;
import com.app.emsx.exceptions.BusinessRuleException;
import com.app.emsx.exceptions.ResourceNotFoundException;
import com.app.emsx.repositories.CustomerRepository;
import com.app.emsx.repositories.OrderRepository;
import com.app.emsx.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Customer with email " + normalizedEmail + " already exists");
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        customer = customerRepository.save(customer);
        // Recargar con relaciones para el response
        customer = customerRepository.findByIdWithOrders(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found after creation"));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAllWithOrders().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (!customer.getEmail().equals(normalizedEmail) && customerRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Customer with email " + normalizedEmail + " already exists");
        }

        customer.setName(request.getName());
        customer.setEmail(normalizedEmail);
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        customer = customerRepository.save(customer);
        // Recargar con relaciones para el response
        customer = customerRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        throw new BusinessRuleException("Customer deletion is not allowed");
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long customerId) {
        Customer customer = customerRepository.findByIdWithOrders(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        List<Order> orders = orderRepository.findOrderHistoryByCustomerId(customerId);
        return orders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse mapToResponse(Customer customer) {
        List<CustomerResponse.OrderSummary> orderSummaries = (customer.getOrders() != null) 
                ? customer.getOrders().stream()
                        .map(order -> CustomerResponse.OrderSummary.builder()
                                .id(order.getId())
                                .date(order.getDate() != null ? order.getDate().toString() : null)
                                .status(order.getStatus() != null ? order.getStatus().name() : null)
                                .total(order.getTotal() != null ? order.getTotal().toString() : null)
                                .build())
                        .collect(Collectors.toList())
                : new java.util.ArrayList<>();

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .orders(orderSummaries)
                .build();
    }

    private OrderResponse mapOrderToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .date(order.getDate())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .total(order.getTotal())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
