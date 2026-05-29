package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    private Long inventoryItemId;
    private Long inventoryId;
    private Long productId;
    private Long cellId;
    private Integer bookQuantity;
    private Integer actualQuantity;
    private Integer difference; // вычисляемое поле
    private LocalDateTime createdAt;
}