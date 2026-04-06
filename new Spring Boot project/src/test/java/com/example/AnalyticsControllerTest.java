package com.example;

import com.example.controller.AnalyticsController;
import com.example.model.User;
import com.example.repository.NotificationRepository;
import com.example.repository.PropertyRepository;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.example.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PropertyRepository propertyRepository;

    @MockBean
    private RecommendationRepository recommendationRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void overviewReturnsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/analytics/overview").with(jwtPrincipal("user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Admin access required"));
    }

    @Test
    public void overviewReturnsStatsForAdmin() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setRole("admin");

        Mockito.when(userRepository.count()).thenReturn(10L);
        Mockito.when(userRepository.countByIsActiveTrue()).thenReturn(8L);
        Mockito.when(userRepository.countByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(2L);
        Mockito.when(propertyRepository.count()).thenReturn(6L);
        Mockito.when(propertyRepository.countByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(3L);
        Mockito.when(recommendationRepository.count()).thenReturn(5L);
        Mockito.when(recommendationRepository.countByIsActiveTrue()).thenReturn(4L);
        Mockito.when(notificationRepository.count()).thenReturn(9L);
        Mockito.when(notificationRepository.countByIsReadFalse()).thenReturn(2L);
        Mockito.when(userRepository.findAll()).thenReturn(List.of(admin));
        Mockito.when(propertyRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/analytics/overview").with(jwtPrincipal("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.users.total").value(10))
                .andExpect(jsonPath("$.data.notifications.unread").value(2));
    }

    private RequestPostProcessor jwtPrincipal(String role) {
        JwtPrincipal principal = new JwtPrincipal(1L, "admin@demo.com", role);
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
