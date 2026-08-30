package com.booking.resourcebooking;

import com.booking.resourcebooking.dto.ResourceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllResources_UserRole_Allowed() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllResources_FilterAvailable() throws Exception {
        mockMvc.perform(get("/resources")
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetResourceById_Exists() throws Exception {
        mockMvc.perform(get("/resources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetResourceById_NotFound() throws Exception {
        mockMvc.perform(get("/resources/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testCreateResource_UserRole_Forbidden() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Unauthorized Room")
                .type("ROOM")
                .pricePerHour(new BigDecimal("50.00"))
                .capacity(5)
                .build();

        mockMvc.perform(post("/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateResource_AdminRole_Success() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("New Auditorium C")
                .type("ROOM")
                .description("Large Auditorium for events")
                .location("Building 3")
                .capacity(100)
                .pricePerHour(new BigDecimal("300.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Auditorium C"))
                .andExpect(jsonPath("$.pricePerHour").value(300.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateResource_AdminRole_Success() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Updated Boardroom A")
                .type("ROOM")
                .description("Updated description")
                .location("Floor 4, Building 1")
                .capacity(25)
                .pricePerHour(new BigDecimal("175.00"))
                .available(true)
                .build();

        mockMvc.perform(put("/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Boardroom A"))
                .andExpect(jsonPath("$.capacity").value(25));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUpdateResource_UserRole_Forbidden() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .name("Hack Attempt")
                .type("ROOM")
                .pricePerHour(new BigDecimal("50.00"))
                .capacity(5)
                .build();

        mockMvc.perform(put("/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testDeleteResource_UserRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/resources/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateResource_ValidationError_MissingName() throws Exception {
        ResourceRequest request = ResourceRequest.builder()
                .type("ROOM")
                .pricePerHour(new BigDecimal("50.00"))
                .capacity(5)
                .build();

        mockMvc.perform(post("/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllResources_Unauthenticated_Unauthorized() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isUnauthorized());
    }
}
