package com.cts.controllers;

import com.cts.dtos.NotificationDto;
import com.cts.dtos.SuccessResponse;
import com.cts.security.JwtTokenProvider;
import com.cts.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    // FIX: Mock JwtTokenProvider to handle Authentication injection and avoid 401
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationDto notificationDto;

    @BeforeEach
    void init() {
        notificationDto = new NotificationDto(1L, "LowStock", "test@example.com", "Stock is low", LocalDateTime.now());

        // FIX: Stub JWT validation to allow the Security Filter to create Authentication
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("admin");
        when(jwtTokenProvider.getRoles(anyString())).thenReturn(List.of("ROLE_ADMIN", "ROLE_SYSTEM"));
    }

    @Test
    void sendNotification() throws Exception {
        when(notificationService.sendNotification(any(NotificationDto.class))).thenReturn(notificationDto);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipient").value("test@example.com"));
    }

    @Test
    void getNotificationById() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(notificationDto);

        mockMvc.perform(get("/api/notifications/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getAllNotifications() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(notificationDto));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void updateNotification() throws Exception {
        when(notificationService.updateNotification(eq(1L), any(NotificationDto.class))).thenReturn(notificationDto);

        mockMvc.perform(put("/api/notifications/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock is low"));
    }

    @Test
    void deleteNotification() throws Exception {
        SuccessResponse successMessage = new SuccessResponse("Notification with ID 1 deleted successfully.");
        when(notificationService.deleteNotification(1L)).thenReturn(successMessage.getMessage());

        mockMvc.perform(delete("/api/notifications/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification with ID 1 deleted successfully."));
    }
}