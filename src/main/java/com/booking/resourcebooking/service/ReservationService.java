package com.booking.resourcebooking.service;

import com.booking.resourcebooking.dto.PagedResponse;
import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.model.ReservationStatus;
import com.booking.resourcebooking.model.User;

import java.math.BigDecimal;

public interface ReservationService {
    ReservationResponse createReservation(ReservationRequest request, User currentUser);
    ReservationResponse getReservationById(Long id, User currentUser);
    PagedResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort,
            User currentUser
    );
    ReservationResponse updateReservation(Long id, ReservationRequest request, User currentUser);
    ReservationResponse updateReservationStatus(Long id, ReservationStatus status, User currentUser);
    void deleteReservation(Long id, User currentUser);
}
