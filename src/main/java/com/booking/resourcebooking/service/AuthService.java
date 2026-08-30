package com.booking.resourcebooking.service;

import com.booking.resourcebooking.dto.AuthResponse;
import com.booking.resourcebooking.dto.LoginRequest;
import com.booking.resourcebooking.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
}
