package com.app.emsx.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Shipment Entity
 * -----------------------------------------------------
 * Representa un envío asociado a una orden
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Column(nullable = false)
    private String carrier;

    @Column
    private String estimatedDeliveryDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    public enum ShipmentStatus {
        PENDING,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        RETURNED
    }
}


