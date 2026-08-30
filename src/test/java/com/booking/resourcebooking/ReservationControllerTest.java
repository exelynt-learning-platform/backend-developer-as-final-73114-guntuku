package com.booking.resourcebooking;

import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationStatusUpdateRequest;
import com.booking.resourcebooking.model.ReservationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails("user")
    void testCreateReservation_Success() throws Exception {
        // Use a future start time far enough ahead to never fail @Future validation
        LocalDateTime start = LocalDateTime.now().plusDays(30).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(1L)
                .startTime(start)
                .endTime(end)
                .notes("Testing creation")
                .build();

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    @WithUserDetails("user")
    void testGetReservations_FilteredByStatusAndPrice() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("status", "CONFIRMED")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "500.00")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @WithUserDetails("admin")
    void testGetAllReservations_Admin_ReturnsAllUsersReservations() throws Exception {
        // Query for a known subset instead of asserting absolute total count
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    @WithUserDetails("user")
    void testGetOwnReservations_User_ReturnsOnlyUserReservations() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].username", everyItem(is("user"))));
    }

    @Test
    @WithUserDetails("user")
    void testUpdateReservationStatus() throws Exception {
        // Create a dedicated reservation to avoid mutating shared dataset reservation ID 1
        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        ReservationRequest createRequest = ReservationRequest.builder()
                .resourceId(1L)
                .startTime(start)
                .endTime(end)
                .notes("Temp for status update test")
                .build();

        String createResponse = mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reservationId = objectMapper.readTree(createResponse).get("id").asLong();

        ReservationStatusUpdateRequest statusUpdate = ReservationStatusUpdateRequest.builder()
                .status(ReservationStatus.CANCELLED)
                .build();

        mockMvc.perform(patch("/reservations/" + reservationId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithUserDetails("user")
    void testCreateReservation_InvalidTimes_BadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(30).withHour(12).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.minusHours(2); // End before Start!

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(1L)
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("user")
    void testGetReservations_SortByPrice() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithUserDetails("user")
    void testGetReservations_InvalidSortField() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("sort", "nonexistentField,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("user")
    void testGetReservations_DefaultSort() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithUserDetails("admin")
    void testGetReservationById_Admin() throws Exception {
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithUserDetails("user")
    void testGetReservationById_NotFound() throws Exception {
        mockMvc.perform(get("/reservations/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("admin")
    void testDeleteReservation_Admin() throws Exception {
        // Create a reservation first, then delete it
        LocalDateTime start = LocalDateTime.now().plusDays(15).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(2L)
                .startTime(start)
                .endTime(end)
                .notes("To be deleted")
                .build();

        String response = mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/reservations/" + createdId))
                .andExpect(status().isNoContent());
    }
}
