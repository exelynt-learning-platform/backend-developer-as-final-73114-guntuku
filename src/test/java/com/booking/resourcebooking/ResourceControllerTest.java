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
}
