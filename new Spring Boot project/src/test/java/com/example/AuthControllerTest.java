package com.example;

import com.example.controller.AuthController;
import com.example.model.User;
<<<<<<< HEAD
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
=======
import com.example.security.JwtUtil;
import com.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@WebMvcTest(controllers = AuthController.class)
>>>>>>> copilot/worktree-2026-04-06T05-00-30
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

<<<<<<< HEAD
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        Mockito.when(jwtUtil.generateToken(Mockito.any(User.class))).thenReturn("mockedToken");
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void testRegisterUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setRole("user");

        Mockito.when(userService.existsByEmail("testuser@example.com")).thenReturn(false);
        Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
            .content("{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"testuser@example.com\",\"password\":\"password\",\"city\":\"Sydney\",\"state\":\"NSW\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.token").value("mockedToken"))
            .andExpect(jsonPath("$.user.email").value("testuser@example.com"));
=======
    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void testRegisterUser() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");

        Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"testuser@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
>>>>>>> copilot/worktree-2026-04-06T05-00-30
    }

    @Test
    public void testLoginUser() throws Exception {
        User user = new User();
<<<<<<< HEAD
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setRole("user");

        Mockito.when(userService.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
            .content("{\"email\":\"testuser@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.token").value("mockedToken"))
            .andExpect(jsonPath("$.user.email").value("testuser@example.com"));
    }
}
=======
        user.setUsername("testuser");
        user.setPassword("password");

        Mockito.when(userService.getAllUsers()).thenReturn(java.util.List.of(user));

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "password");

        String token = jwtUtil.generateToken("testuser");
        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token, "testuser"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }
}
>>>>>>> copilot/worktree-2026-04-06T05-00-30
