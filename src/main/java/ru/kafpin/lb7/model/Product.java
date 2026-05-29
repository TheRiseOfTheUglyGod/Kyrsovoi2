package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long productId;
    private String name;
    private String article;
    private String unit;
    private String description;
    private String photoPath;
    private Integer minStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}