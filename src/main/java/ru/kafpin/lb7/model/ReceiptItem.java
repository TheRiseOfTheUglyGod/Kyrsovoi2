package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItem {
    private Long receiptItemId;
    private Long receiptId;
    private Long productId;
    private Integer quantity;
    private BigDecimal purchasePrice;
    private Long cellId;
    private LocalDateTime createdAt;
}