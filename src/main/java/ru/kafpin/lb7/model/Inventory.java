package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    private Long inventoryId;
    private LocalDate inventoryDate;
    private String status; // 'in_progress' или 'completed'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}