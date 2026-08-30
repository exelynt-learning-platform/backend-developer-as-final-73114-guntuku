package com.booking.resourcebooking;

import com.booking.resourcebooking.dto.LoginRequest;
import com.booking.resourcebooking.dto.RegisterRequest;
import com.booking.resourcebooking.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginSuccess_Admin() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("admin123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void testLoginSuccess_User() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("user")
                .password("user123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void testLoginFailure_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testRegisterNewUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .role(Role.ROLE_USER)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }
}
