package com.booking.resourcebooking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {

    private Long id;
    private String name;
    private String type;
    private String description;
    private String location;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
