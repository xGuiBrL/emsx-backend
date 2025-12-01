package com.app.emsx.serviceimpls;

import com.app.emsx.dtos.product.ProductRequest;
import com.app.emsx.dtos.product.ProductResponse;
import com.app.emsx.dtos.stock.StockResponse;
import com.app.emsx.entities.Product;
import com.app.emsx.entities.Stock;
import com.app.emsx.exceptions.BusinessRuleException;
import com.app.emsx.exceptions.ResourceNotFoundException;
import com.app.emsx.repositories.ProductRepository;
import com.app.emsx.repositories.StockRepository;
import com.app.emsx.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String normalizedSku = normalizeSku(request.getSku());
        if (productRepository.existsBySku(normalizedSku)) {
            throw new BusinessRuleException("Ya existe un producto con el SKU " + normalizedSku);
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .sku(normalizedSku)
                .build();

        product = productRepository.save(product);

        // Crear stock inicial
        Stock stock = Stock.builder()
                .quantity(request.getInitialStock())
                .reservedQuantity(0)
                .product(product)
                .build();

        stock = stockRepository.save(stock);
        product.setStock(stock);

        // Recargar con relaciones para el response
        product = productRepository.findByIdWithStock(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found after creation"));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdWithStock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        ensureProductIsActive(product);
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAllWithStock().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(String category) {
        // Usar findAllWithStock y filtrar por categoría en memoria para evitar lazy loading
        return productRepository.findAllWithStock().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().equals(category))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findByIdWithStock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        ensureProductIsActive(product);

        String normalizedSku = normalizeSku(request.getSku());
        if (!product.getSku().equals(normalizedSku) && productRepository.existsBySku(normalizedSku)) {
            throw new BusinessRuleException("Ya existe un producto con el SKU " + normalizedSku);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setSku(normalizedSku);

        product = productRepository.save(product);

        Stock stock = product.getStock();
        if (stock == null) {
            stock = stockRepository.findByProductId(product.getId())
                    .orElse(Stock.builder()
                            .product(product)
                            .reservedQuantity(0)
                            .quantity(request.getInitialStock())
                            .build());
        }

        Integer updatedQuantity = request.getInitialStock();
        if (updatedQuantity != null) {
            int reserved = stock.getReservedQuantity() == null ? 0 : stock.getReservedQuantity();
            if (updatedQuantity < reserved) {
                throw new BusinessRuleException("El stock disponible no puede ser menor al stock reservado");
            }
            stock.setQuantity(updatedQuantity);
        }
        stockRepository.save(stock);

        // Recargar con relaciones para el response
        product = productRepository.findByIdWithStock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (product.getStatus() == Product.ProductStatus.INACTIVE) {
            return;
        }
        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        ensureProductIsActive(product);

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product id: " + productId));

        return StockResponse.builder()
                .id(stock.getId())
                .quantity(stock.getQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getQuantity() - stock.getReservedQuantity())
                .product(StockResponse.ProductInfo.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .sku(product.getSku())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        // Obtener todos los productos con stock y filtrar los disponibles
        return productRepository.findAllWithStock().stream()
                .filter(this::isActive)
                .filter(p -> {
                    if (p.getStock() == null) return false;
                    int available = p.getStock().getQuantity() - p.getStock().getReservedQuantity();
                    return available > 0;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.StockInfo stockInfo = null;
        if (product.getStock() != null) {
            Stock stock = product.getStock();
            stockInfo = ProductResponse.StockInfo.builder()
                    .quantity(stock.getQuantity())
                    .reservedQuantity(stock.getReservedQuantity())
                    .availableQuantity(stock.getQuantity() - stock.getReservedQuantity())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .sku(product.getSku())
                .status(product.getStatus() != null ? product.getStatus().name() : Product.ProductStatus.ACTIVE.name())
                .stock(stockInfo)
                .build();
    }

    private boolean isActive(Product product) {
        return product.getStatus() == null || product.getStatus() == Product.ProductStatus.ACTIVE;
    }

    private void ensureProductIsActive(Product product) {
        if (!isActive(product)) {
            throw new ResourceNotFoundException("Product not found with id: " + product.getId());
        }
    }

    private String normalizeSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase();
    }
}

