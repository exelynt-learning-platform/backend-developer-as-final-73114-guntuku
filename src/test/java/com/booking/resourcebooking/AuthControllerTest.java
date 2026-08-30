package com.booking.resourcebooking;

import com.booking.resourcebooking.dto.LoginRequest;
import com.booking.resourcebooking.dto.RegisterRequest;
import com.booking.resourcebooking.model.Role;
import com.booking.resourcebooking.model.User;
import com.booking.resourcebooking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminUsername;
    private String adminPassword;
    private String userUsername;
    private String userPassword;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        adminUsername = "testadmin_" + uniqueId;
        adminPassword = "adminPass123";
        userUsername = "testuser_" + uniqueId;
        userPassword = "userPass123";

        // Create test admin user programmatically
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminUsername + "@test.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
        }

        // Create test regular user programmatically
        if (!userRepository.existsByUsername(userUsername)) {
            User user = User.builder()
                    .username(userUsername)
                    .email(userUsername + "@test.com")
                    .password(passwordEncoder.encode(userPassword))
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    void testLoginSuccess_Admin() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(adminUsername)
                .password(adminPassword)
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
                .usernameOrEmail(userUsername)
                .password(userPassword)
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
                .usernameOrEmail(adminUsername)
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
        String regUsername = "newreg_" + UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(regUsername)
                .email(regUsername + "@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value(regUsername))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(userUsername)
                .email("unique_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        String existingEmail = userUsername + "@test.com";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("unique_" + UUID.randomUUID().toString().substring(0, 8))
                .email(existingEmail)
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email address is already in use!"));
    }

    @Test
    void testRegisterWithAdminRole_StillAssignedUserRole() throws Exception {
        String regUsername = "sneaky_" + UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(regUsername)
                .email(regUsername + "@test.com")
                .password("password123")
                .role(Role.ROLE_ADMIN)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void testRegisterValidation_BlankUsername() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("")
                .email("valid@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void testRegisterValidation_InvalidEmail() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("validuser123")
                .email("not-an-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}
