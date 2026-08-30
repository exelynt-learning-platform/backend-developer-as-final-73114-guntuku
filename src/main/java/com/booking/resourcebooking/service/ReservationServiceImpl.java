package com.booking.resourcebooking.service;

import com.booking.resourcebooking.dto.PagedResponse;
import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.exception.UnauthorizedAccessException;
import com.booking.resourcebooking.model.*;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ReservationSpecification;
import com.booking.resourcebooking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User currentUser) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));

        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource is currently unavailable for booking");
        }

        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                resource.getId(), request.getStartTime(), request.getEndTime()
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Resource is already booked for the selected time slot");
        }

        BigDecimal calculatedPrice = calculateTotalPrice(resource.getPricePerHour(), request.getStartTime(), request.getEndTime());

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(currentUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING)
                .price(calculatedPrice)
                .notes(request.getNotes())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        verifyOwnershipOrAdmin(reservation, currentUser);

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort,
            User currentUser
    ) {
        Long userIdFilter = (currentUser.getRole() == Role.ROLE_ADMIN) ? null : currentUser.getId();

        Sort sortObj = parseSortString(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Reservation> spec = ReservationSpecification.filterReservations(
                status, minPrice, maxPrice, userIdFilter
        );

        Page<Reservation> reservationPage = reservationRepository.findAll(spec, pageable);

        List<ReservationResponse> content = reservationPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReservationResponse>builder()
                .content(content)
                .pageNumber(reservationPage.getNumber())
                .pageSize(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .last(reservationPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        verifyOwnershipOrAdmin(reservation, currentUser);

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));

        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                resource.getId(), request.getStartTime(), request.getEndTime()
        ).stream().filter(r -> !r.getId().equals(id)).collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Resource is already booked for the selected time slot");
        }

        BigDecimal calculatedPrice = calculateTotalPrice(resource.getPricePerHour(), request.getStartTime(), request.getEndTime());

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(calculatedPrice);
        reservation.setNotes(request.getNotes());

        Reservation updated = reservationRepository.save(reservation);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservationStatus(Long id, ReservationStatus status, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        verifyOwnershipOrAdmin(reservation, currentUser);

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        verifyOwnershipOrAdmin(reservation, currentUser);

        reservationRepository.delete(reservation);
    }

    private void verifyOwnershipOrAdmin(Reservation reservation, User currentUser) {
        if (currentUser.getRole() != Role.ROLE_ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access or modify this reservation");
        }
    }

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final int PRICE_SCALE = 2;

    private BigDecimal calculateTotalPrice(BigDecimal pricePerHour, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("Start time must be before end time");
        }
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(MINUTES_PER_HOUR, PRICE_SCALE, RoundingMode.HALF_UP);
        return pricePerHour.multiply(hours).setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
            "id", "startTime", "endTime", "status", "price", "notes", "createdAt", "updatedAt"
    );

    private Sort parseSortString(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new IllegalArgumentException("Invalid sort field: " + property + ". Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .resourceType(reservation.getResource().getType() != null ? reservation.getResource().getType().name() : null)
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .userEmail(reservation.getUser().getEmail())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .price(reservation.getPrice())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
