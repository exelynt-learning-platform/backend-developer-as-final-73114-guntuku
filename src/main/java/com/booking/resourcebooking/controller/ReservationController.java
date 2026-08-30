package com.booking.resourcebooking.controller;

import com.booking.resourcebooking.dto.PagedResponse;
import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.dto.ReservationStatusUpdateRequest;
import com.booking.resourcebooking.model.ReservationStatus;
import com.booking.resourcebooking.model.User;
import com.booking.resourcebooking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints for booking management, filtering, status updates, and ownership enforcement")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create Reservation", description = "Creates a new reservation for a resource. User identity is taken strictly from JWT.")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ReservationResponse response = reservationService.createReservation(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get Filtered & Paginated Reservations",
            description = "Retrieves reservations with optional status, minPrice, maxPrice filtering, pagination (page, size), and sorting. USER can see only their own reservations, ADMIN can see all."
    )
    public ResponseEntity<PagedResponse<ReservationResponse>> getAllReservations(
            @RequestParam(name = "status", required = false) ReservationStatus status,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc")
            @Parameter(description = "Sorting criteria in format: property(,asc|desc). Allowed properties match entity fields (e.g., id, startTime, endTime, status, price, createdAt).")
            String sort,
            @AuthenticationPrincipal User currentUser
    ) {
        PagedResponse<ReservationResponse> response = reservationService.getAllReservations(
                status, minPrice, maxPrice, page, size, sort, currentUser
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Reservation by ID", description = "Retrieves a single reservation by ID. Restricted to owner or ADMIN.")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        ReservationResponse response = reservationService.getReservationById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Reservation", description = "Updates an existing reservation's resource or timing. Restricted to owner or ADMIN.")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ReservationResponse response = reservationService.updateReservation(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Reservation Status", description = "Updates reservation status (PENDING, CONFIRMED, CANCELLED). Restricted to owner or ADMIN.")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete / Cancel Reservation", description = "Deletes a reservation by ID. Restricted to owner or ADMIN.")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        reservationService.deleteReservation(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
