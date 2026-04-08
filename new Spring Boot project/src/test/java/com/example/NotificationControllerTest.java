package com.example;

import com.example.controller.NotificationController;
import com.example.model.Notification;
import com.example.model.User;
import com.example.repository.NotificationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.example.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    public void getNotificationsReturnsList() throws Exception {
        Notification notification = notification(1L, 1L, "N1", false);
        Mockito.when(notificationRepository.findByOwnerIdAndIsReadOrderByCreatedAtDesc(1L, false))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications")
                        .param("unreadOnly", "true")
                        .with(Objects.requireNonNull(jwtPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.notifications[0].title").value("N1"));
    }

    @Test
    @SuppressWarnings("null")
    public void createNotificationReturnsSavedNotification() throws Exception {
        User owner = new User();
        owner.setId(1L);

        Notification notification = notification(10L, 1L, "Title", false);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.doReturn(notification).when(notificationRepository).save(Mockito.any(Notification.class));

        mockMvc.perform(post("/api/notifications")
                        .with(Objects.requireNonNull(jwtPrincipal()))
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"title\":\"Title\",\"message\":\"Body\",\"type\":\"reminder\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminder created successfully"))
                .andExpect(jsonPath("$.notification.title").value("Title"));
    }

    @Test
    public void markReadReturnsNotFound() throws Exception {
        Mockito.when(notificationRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/notifications/99/read").with(Objects.requireNonNull(jwtPrincipal())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    private RequestPostProcessor jwtPrincipal() {
        JwtPrincipal principal = new JwtPrincipal(1L, "user@demo.com", "user");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    private Notification notification(Long id, Long userId, String title, boolean isRead) {
        User owner = new User();
        owner.setId(userId);

        Notification notification = new Notification();
        notification.setId(id);
        notification.setOwner(owner);
        notification.setTitle(title);
        notification.setMessage("Body");
        notification.setType("reminder");
        notification.setIsRead(isRead);
        notification.setDueAt(LocalDateTime.now().plusDays(1));
        return notification;
    }
}

