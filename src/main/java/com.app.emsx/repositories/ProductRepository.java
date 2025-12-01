package com.app.emsx.repositories;

import com.app.emsx.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByCategory(String category);
    boolean existsBySku(String sku);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.stock")
    List<Product> findAllWithStock();
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.stock WHERE p.id = :id")
    Optional<Product> findByIdWithStock(@Param("id") Long id);
}


