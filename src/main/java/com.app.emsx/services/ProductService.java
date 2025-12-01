package com.app.emsx.services;

import com.app.emsx.dtos.product.ProductRequest;
import com.app.emsx.dtos.product.ProductResponse;
import com.app.emsx.dtos.stock.StockResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    List<ProductResponse> getAll();
    List<ProductResponse> getByCategory(String category);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    StockResponse getStock(Long productId);
    List<ProductResponse> getAvailableProducts();
}


