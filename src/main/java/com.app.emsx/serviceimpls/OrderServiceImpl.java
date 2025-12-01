package com.app.emsx.serviceimpls;

import com.app.emsx.dtos.order.OrderItemRequest;
import com.app.emsx.dtos.order.OrderRequest;
import com.app.emsx.dtos.order.OrderResponse;
import com.app.emsx.entities.*;
import com.app.emsx.exceptions.BusinessRuleException;
import com.app.emsx.exceptions.ResourceNotFoundException;
import com.app.emsx.repositories.*;
import com.app.emsx.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockRepository stockRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        // Validar cliente
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        // Crear orden
        Order order = Order.builder()
                .date(LocalDateTime.now())
                .status(Order.OrderStatus.PENDING)
                .total(BigDecimal.ZERO)
                .customer(customer)
                .build();

        order = orderRepository.save(order);

        // Procesar items y calcular total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = loadActiveProduct(itemRequest.getProductId());

            // Validar stock disponible
            Stock stock = stockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product id: " + product.getId()));

            int availableQuantity = stock.getQuantity() - stock.getReservedQuantity();
            if (availableQuantity < itemRequest.getQuantity()) {
                throw new BusinessRuleException("No hay stock suficiente para " + product.getName()
                        + ". Disponible: " + availableQuantity + ", Solicitado: " + itemRequest.getQuantity());
            }

            // Calcular subtotal
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            // Crear OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .quantity(itemRequest.getQuantity())
                    .subtotal(subtotal)
                    .order(order)
                    .product(product)
                    .build();

            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);

            // Reducir stock
            stock.setQuantity(stock.getQuantity() - itemRequest.getQuantity());
            stockRepository.save(stock);

            total = total.add(subtotal);
        }

        // Actualizar total de la orden (mantener en PENDING, no crear shipment)
        order.setTotal(total);
        // Order se mantiene en PENDING, no se crea shipment automáticamente
        order = orderRepository.save(order);

        // Recargar con relaciones para el response
        order = orderRepository.findByIdWithRelations(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found after creation"));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAllWithRelations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse addOrderItems(Long orderId, List<OrderItemRequest> items) {
        Order order = orderRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Cannot add items to order with status: " + order.getStatus());
        }

        BigDecimal additionalTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : items) {
            Product product = loadActiveProduct(itemRequest.getProductId());

            Stock stock = stockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product id: " + product.getId()));

            int availableQuantity = stock.getQuantity() - stock.getReservedQuantity();
            if (availableQuantity < itemRequest.getQuantity()) {
                throw new BusinessRuleException("No hay stock suficiente para " + product.getName()
                        + ". Disponible: " + availableQuantity + ", Solicitado: " + itemRequest.getQuantity());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .quantity(itemRequest.getQuantity())
                    .subtotal(subtotal)
                    .order(order)
                    .product(product)
                    .build();

            orderItemRepository.save(orderItem);
            order.getOrderItems().add(orderItem);

            stock.setQuantity(stock.getQuantity() - itemRequest.getQuantity());
            stockRepository.save(stock);

            additionalTotal = additionalTotal.add(subtotal);
        }

        order.setTotal(order.getTotal().add(additionalTotal));
        order = orderRepository.save(order);

        // Recargar con relaciones para el response
        order = orderRepository.findByIdWithRelations(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found after update"));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Validar que el estado no sea inválido
        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid order status: " + status);
        }

        // Validar estados inalterables
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot modify order with status CANCELLED");
        }

        // Validar que no se pueda cambiar si está en CONFIRMED (excepto reglas automáticas)
        if (order.getStatus() == Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Cannot modify order status once it is CONFIRMED");
        }

        // Validar que solo se permitan los estados especificados: PENDING, SHIPPED, CANCELLED, CONFIRMED
        if (newStatus != Order.OrderStatus.PENDING &&
                newStatus != Order.OrderStatus.SHIPPED &&
                newStatus != Order.OrderStatus.CANCELLED &&
                newStatus != Order.OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Only PENDING, SHIPPED, CANCELLED, and CONFIRMED statuses are allowed");
        }

        if (order.getStatus() == Order.OrderStatus.SHIPPED && newStatus == Order.OrderStatus.PENDING) {
            throw new BusinessRuleException("Cannot return an order to PENDING after it was SHIPPED");
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Lógica automática según el nuevo estado
        if (newStatus == Order.OrderStatus.SHIPPED) {
            // Cuando Order cambia a SHIPPED → crear/actualizar Shipment con OUT_FOR_DELIVERY
            Shipment shipment = order.getShipment();
            if (shipment == null) {
                // Crear nuevo shipment
                String trackingCode = "TRK-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                shipment = Shipment.builder()
                        .trackingCode(trackingCode)
                        .status(Shipment.ShipmentStatus.OUT_FOR_DELIVERY)
                        .carrier("Standard Carrier")
                        .estimatedDeliveryDate(java.time.LocalDate.now().plusDays(3).toString())
                        .order(order)
                        .build();
                shipment = shipmentRepository.save(shipment);
                order.setShipment(shipment);
            } else {
                // Actualizar shipment existente
                shipment.setStatus(Shipment.ShipmentStatus.OUT_FOR_DELIVERY);
                shipmentRepository.save(shipment);
            }
        } else if (newStatus == Order.OrderStatus.CONFIRMED) {
            // Cuando Order pasa a CONFIRMED → Shipment pasa a DELIVERED (o se crea si no existe)
            Shipment shipment = order.getShipment();
            if (shipment == null) {
                // Si no existe shipment, crear uno nuevo con estado DELIVERED
                String trackingCode = "TRK-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                shipment = Shipment.builder()
                        .trackingCode(trackingCode)
                        .status(Shipment.ShipmentStatus.DELIVERED)
                        .carrier("Standard Carrier")
                        .estimatedDeliveryDate(java.time.LocalDate.now().toString())
                        .order(order)
                        .build();
                shipment = shipmentRepository.save(shipment);
                order.setShipment(shipment);
            } else if (shipment.getStatus() != Shipment.ShipmentStatus.RETURNED) {
                // Si existe shipment, actualizar a DELIVERED
                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                shipmentRepository.save(shipment);
            }
        } else if (newStatus == Order.OrderStatus.CANCELLED) {
            Shipment shipment = order.getShipment();
            if (shipment != null) {
                shipment.setStatus(Shipment.ShipmentStatus.RETURNED);
                shipmentRepository.save(shipment);
            }
        }

        order = orderRepository.save(order);

        // Recargar con relaciones para el response
        order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Restaurar stock si la orden está en estado PENDING o CONFIRMED
        if (order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.CONFIRMED) {
            List<OrderItem> orderItems = order.getOrderItems() != null ? order.getOrderItems() : new java.util.ArrayList<>();
            for (OrderItem item : orderItems) {
                Stock stock = stockRepository.findByProductId(item.getProduct().getId())
                        .orElse(null);
                if (stock != null) {
                    stock.setQuantity(stock.getQuantity() + item.getQuantity());
                    stockRepository.save(stock);
                }
            }
        }

        orderRepository.delete(order);
    }

    private Product loadActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        if (product.getStatus() == Product.ProductStatus.INACTIVE) {
            throw new BusinessRuleException("El producto " + product.getName() + " está dado de baja");
        }
        return product;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemInfo> itemInfos = (order.getOrderItems() != null)
                ? order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemInfo.builder()
                        .id(item.getId())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .product(OrderResponse.OrderItemInfo.ProductInfo.builder()
                                .id(item.getProduct().getId())
                                .name(item.getProduct().getName())
                                .sku(item.getProduct().getSku())
                                .price(item.getProduct().getPrice())
                                .build())
                        .build())
                .collect(Collectors.toList())
                : new java.util.ArrayList<>();

        OrderResponse.ShipmentInfo shipmentInfo = null;
        if (order.getShipment() != null) {
            Shipment shipment = order.getShipment();
            shipmentInfo = OrderResponse.ShipmentInfo.builder()
                    .id(shipment.getId())
                    .trackingCode(shipment.getTrackingCode())
                    .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                    .carrier(shipment.getCarrier())
                    .build();
        }

        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .date(order.getDate())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .total(order.getTotal())
                .items(itemInfos)
                .shipment(shipmentInfo);

        if (order.getCustomer() != null) {
            builder.customer(OrderResponse.CustomerInfo.builder()
                    .id(order.getCustomer().getId())
                    .name(order.getCustomer().getName())
                    .email(order.getCustomer().getEmail())
                    .build());
        }

        return builder.build();
    }
}