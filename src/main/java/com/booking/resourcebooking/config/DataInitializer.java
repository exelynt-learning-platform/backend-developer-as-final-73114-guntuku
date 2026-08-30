package com.booking.resourcebooking.config;

import com.booking.resourcebooking.model.*;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ResourceRepository;
import com.booking.resourcebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (resourceRepository.count() == 0) {
            seedResources();
        }
        if (reservationRepository.count() == 0) {
            seedReservations();
        }
    }

    /**
     * Seeds initial user accounts if none exist in the database.
     */
    private void seedUsers() {
        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)
                .build();

        User user1 = User.builder()
                .username("user")
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(admin);
        userRepository.save(user1);
        userRepository.save(user2);
    }

    /**
     * Seeds initial catalog resources if none exist in the database.
     */
    private void seedResources() {
        Resource r1 = Resource.builder()
                .name("Executive Boardroom A")
                .type(ResourceType.ROOM)
                .description("High-end boardroom equipped with video conferencing setup.")
                .location("Floor 4, Building 1")
                .capacity(20)
                .pricePerHour(new BigDecimal("150.00"))
                .available(true)
                .build();

        Resource r2 = Resource.builder()
                .name("Tech Conference Room B")
                .type(ResourceType.ROOM)
                .description("Modern room ideal for team huddles and workshops.")
                .location("Floor 2, Building 1")
                .capacity(10)
                .pricePerHour(new BigDecimal("75.50"))
                .available(true)
                .build();

        Resource r3 = Resource.builder()
                .name("Electric Shuttle Van")
                .type(ResourceType.VEHICLE)
                .description("8-seater electric van for campus transit.")
                .location("Main Garage")
                .capacity(8)
                .pricePerHour(new BigDecimal("120.00"))
                .available(true)
                .build();

        Resource r4 = Resource.builder()
                .name("4K Laser Projector")
                .type(ResourceType.EQUIPMENT)
                .description("Ultra-bright 4K projector for presentations.")
                .location("IT Storage Room")
                .capacity(1)
                .pricePerHour(new BigDecimal("35.00"))
                .available(true)
                .build();

        resourceRepository.save(r1);
        resourceRepository.save(r2);
        resourceRepository.save(r3);
        resourceRepository.save(r4);
    }

    /**
     * Seeds initial sample reservations if none exist in the database.
     */
    private void seedReservations() {
        User user1 = userRepository.findByUsername("user").orElse(null);
        User user2 = userRepository.findByUsername("user2").orElse(null);
        Resource r1 = resourceRepository.findByName("Executive Boardroom A").orElse(null);
        Resource r2 = resourceRepository.findByName("Tech Conference Room B").orElse(null);
        Resource r3 = resourceRepository.findByName("Electric Shuttle Van").orElse(null);
        Resource r4 = resourceRepository.findByName("4K Laser Projector").orElse(null);

        if (user1 == null || user2 == null || r1 == null || r2 == null || r3 == null || r4 == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Reservation res1 = Reservation.builder()
                .resource(r1)
                .user(user1)
                .startTime(now.plusDays(1).withHour(9).withMinute(0))
                .endTime(now.plusDays(1).withHour(11).withMinute(0))
                .status(ReservationStatus.CONFIRMED)
                .price(new BigDecimal("300.00"))
                .notes("Quarterly planning meeting")
                .build();

        Reservation res2 = Reservation.builder()
                .resource(r2)
                .user(user1)
                .startTime(now.plusDays(2).withHour(14).withMinute(0))
                .endTime(now.plusDays(2).withHour(16).withMinute(0))
                .status(ReservationStatus.PENDING)
                .price(new BigDecimal("151.00"))
                .notes("Sprint demo session")
                .build();

        Reservation res3 = Reservation.builder()
                .resource(r3)
                .user(user2)
                .startTime(now.plusDays(3).withHour(10).withMinute(0))
                .endTime(now.plusDays(3).withHour(13).withMinute(0))
                .status(ReservationStatus.CONFIRMED)
                .price(new BigDecimal("360.00"))
                .notes("Client site visit transit")
                .build();

        Reservation res4 = Reservation.builder()
                .resource(r4)
                .user(user2)
                .startTime(now.plusDays(4).withHour(11).withMinute(0))
                .endTime(now.plusDays(4).withHour(13).withMinute(0))
                .status(ReservationStatus.CANCELLED)
                .price(new BigDecimal("70.00"))
                .notes("Cancelled due to schedule change")
                .build();

        reservationRepository.save(res1);
        reservationRepository.save(res2);
        reservationRepository.save(res3);
        reservationRepository.save(res4);
    }
}
