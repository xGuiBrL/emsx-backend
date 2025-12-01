package com.app.emsx.repositories;

import com.app.emsx.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.shipment WHERE o.customer.id = :customerId ORDER BY o.date DESC")
    List<Order> findOrderHistoryByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.shipment")
    List<Order> findAllWithRelations();
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.shipment WHERE o.id = :id")
    Optional<Order> findByIdWithRelations(@Param("id") Long id);
}


