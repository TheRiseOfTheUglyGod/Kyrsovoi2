package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentItem {
    private Long shipmentItemId;
    private Long shipmentId;
    private Long productId;
    private Integer quantity;
    private Long cellId;
    private LocalDateTime createdAt;
}