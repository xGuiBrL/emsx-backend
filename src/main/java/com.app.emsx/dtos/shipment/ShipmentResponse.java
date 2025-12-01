package com.app.emsx.dtos.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String trackingCode;
    private String status;
    private String carrier;
    private String estimatedDeliveryDate;
    private OrderInfo order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Long id;
        private String date;
        private String status;
        private String total;
    }
}


