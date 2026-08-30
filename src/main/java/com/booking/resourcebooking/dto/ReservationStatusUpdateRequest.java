package com.booking.resourcebooking.dto;

import com.booking.resourcebooking.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatusUpdateRequest {

    @NotNull(message = "Reservation status is required")
    private ReservationStatus status;
}
