package ru.kafpin.lb7.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageCell {
    private Long cellId;
    private String zone;
    private Integer rowNum;
    private String rack;
    private Integer cellNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}