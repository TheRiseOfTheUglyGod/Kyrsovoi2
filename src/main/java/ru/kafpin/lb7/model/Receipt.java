package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private Long receiptId;
    private LocalDate receiptDate;
    private Long supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}