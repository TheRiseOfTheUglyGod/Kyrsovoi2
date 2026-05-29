package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    private Long stockId;
    private Long productId;
    private Long cellId;
    private Integer quantity;
    private LocalDateTime updatedAt;
}