package com.booking.resourcebooking.dto;

import com.booking.resourcebooking.model.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private Long userId;
    private String username;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal price;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
