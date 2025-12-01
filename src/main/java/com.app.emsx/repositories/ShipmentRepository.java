package com.app.emsx.repositories;

import com.app.emsx.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingCode(String trackingCode);
    
    @Query("SELECT s FROM Shipment s JOIN FETCH s.order WHERE s.order.id = :orderId")
    Optional<Shipment> findByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT DISTINCT s FROM Shipment s LEFT JOIN FETCH s.order")
    List<Shipment> findAllWithOrder();
    
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.order WHERE s.id = :id")
    Optional<Shipment> findByIdWithOrder(@Param("id") Long id);
}


