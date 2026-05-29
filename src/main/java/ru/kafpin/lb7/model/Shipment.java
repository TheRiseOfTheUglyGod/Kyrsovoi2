package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    private Long shipmentId;
    private LocalDate shipmentDate;
    private String customer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}