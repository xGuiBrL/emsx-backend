package com.app.emsx.repositories;

import com.app.emsx.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s FROM Stock s WHERE s.product.id = :productId")
    Optional<Stock> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT s FROM Stock s WHERE s.quantity > 0")
    List<Stock> findAvailableStocks();
    
    @Query("SELECT s FROM Stock s WHERE s.quantity > :minQuantity")
    List<Stock> findStocksWithQuantityGreaterThan(@Param("minQuantity") Integer minQuantity);
}


