package com.example.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
