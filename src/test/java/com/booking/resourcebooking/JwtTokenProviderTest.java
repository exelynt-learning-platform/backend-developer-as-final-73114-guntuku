package com.booking.resourcebooking;

import com.booking.resourcebooking.model.Role;
import com.booking.resourcebooking.model.User;
import com.booking.resourcebooking.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "9a2f8c4e7b1d3a5f8e6c4b2a0d9e8f7c6b5a4d3e2f1a0b9c8d7e6f5a4b3c2d1e");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", 3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromJWT(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalidTokenString"));
    }
}
