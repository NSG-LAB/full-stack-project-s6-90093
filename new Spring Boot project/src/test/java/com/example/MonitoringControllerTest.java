package com.example;

import com.example.controller.MonitoringController;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.example.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MonitoringController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void metricsReturnsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/monitoring/metrics").with(Objects.requireNonNull(jwtPrincipal("user"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Admin access required"));
    }

    @Test
    public void logsReturnsSuccessForAdmin() throws Exception {
        mockMvc.perform(get("/api/monitoring/logs").with(Objects.requireNonNull(jwtPrincipal("admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void pm2StatusReturnsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/monitoring/pm2-status").with(Objects.requireNonNull(jwtPrincipal("user"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private RequestPostProcessor jwtPrincipal(String role) {
        JwtPrincipal principal = new JwtPrincipal(1L, "user@demo.com", role);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        );

        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }
}

