package com.app.emsx.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private LocalDateTime date;
    private String status;
    private BigDecimal total;
    private CustomerInfo customer;
    private List<OrderItemInfo> items;
    private ShipmentInfo shipment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long id;
        private Integer quantity;
        private BigDecimal subtotal;
        private ProductInfo product;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductInfo {
            private Long id;
            private String name;
            private String sku;
            private BigDecimal price;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentInfo {
        private Long id;
        private String trackingCode;
        private String status;
        private String carrier;
    }
}


