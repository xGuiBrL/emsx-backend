package com.app.emsx.serviceimpls;

import com.app.emsx.dtos.shipment.ShipmentRequest;
import com.app.emsx.dtos.shipment.ShipmentResponse;
import com.app.emsx.entities.Order;
import com.app.emsx.entities.Shipment;
import com.app.emsx.exceptions.BusinessRuleException;
import com.app.emsx.exceptions.ResourceNotFoundException;
import com.app.emsx.repositories.OrderRepository;
import com.app.emsx.repositories.ShipmentRepository;
import com.app.emsx.services.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        Order order = orderRepository.findByIdWithRelations(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Verificar si ya existe un shipment para esta orden
        if (order.getShipment() != null) {
            throw new BusinessRuleException("Order already has a shipment");
        }

        // Generar código de tracking único
        String trackingCode = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Shipment shipment = Shipment.builder()
                .trackingCode(trackingCode)
                .status(Shipment.ShipmentStatus.PENDING)
                .carrier(request.getCarrier())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .order(order)
                .build();

        shipment = shipmentRepository.save(shipment);
        order.setShipment(shipment);
        order.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(order);

        // Recargar con relaciones para el response
        shipment = shipmentRepository.findByIdWithOrder(shipment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found after creation"));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getById(Long id) {
        Shipment shipment = shipmentRepository.findByIdWithOrder(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAll() {
        return shipmentRepository.findAllWithOrder().stream()
                .map(this::mapToResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(Long id, String status) {
        Shipment shipment = shipmentRepository.findByIdWithOrder(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        // Validar que no se pueda cambiar si está en RETURNED
        if (shipment.getStatus() == Shipment.ShipmentStatus.RETURNED) {
            throw new BusinessRuleException("Cannot modify shipment with status RETURNED");
        }

        Shipment.ShipmentStatus newStatus;
        try {
            newStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid shipment status: " + status);
        }

        // Validar que solo se permita cambiar a RETURNED cuando el Order está en CONFIRMED
        if (newStatus == Shipment.ShipmentStatus.RETURNED) {
            Order order = shipment.getOrder();
            if (order == null) {
                throw new BusinessRuleException("Shipment must be associated with an order to mark as RETURNED");
            }
            if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
                throw new BusinessRuleException("Can only mark shipment as RETURNED when order is in CONFIRMED status");
            }
        }

        shipment.setStatus(newStatus);
        shipment = shipmentRepository.save(shipment);

        // Cuando Shipment pasa a RETURNED → Order pasa a CANCELLED
        if (newStatus == Shipment.ShipmentStatus.RETURNED) {
            Order order = shipment.getOrder();
            if (order != null && order.getStatus() != Order.OrderStatus.CANCELLED) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }

        // Recargar con relaciones para el response
        shipment = shipmentRepository.findByIdWithOrder(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse trackByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order id: " + orderId));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse trackByTrackingCode(String trackingCode) {
        Shipment shipment = shipmentRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking code: " + trackingCode));
        // Recargar con relaciones para el response
        final Long shipmentId = shipment.getId();
        shipment = shipmentRepository.findByIdWithOrder(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shipment shipment = shipmentRepository.findByIdWithOrder(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        // Remover referencia de la orden
        Order order = shipment.getOrder();
        if (order != null) {
            order.setShipment(null);
            orderRepository.save(order);
        }

        shipmentRepository.delete(shipment);
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse.ShipmentResponseBuilder builder = ShipmentResponse.builder()
                .id(shipment.getId())
                .trackingCode(shipment.getTrackingCode())
                .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                .carrier(shipment.getCarrier())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        
        // Manejar orden de forma segura
        if (shipment.getOrder() != null) {
            Order order = shipment.getOrder();
            builder.order(ShipmentResponse.OrderInfo.builder()
                    .id(order.getId())
                    .date(order.getDate() != null ? order.getDate().toString() : null)
                    .status(order.getStatus() != null ? order.getStatus().name() : null)
                    .total(order.getTotal() != null ? order.getTotal().toString() : null)
                    .build());
        }
        
        return builder.build();
    }
}

