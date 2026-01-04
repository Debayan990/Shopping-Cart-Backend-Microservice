package com.cts.controller;

import com.cts.dtos.AuditLogDto;
import com.cts.dtos.SuccessResponse;
import com.cts.security.JwtTokenProvider;
import com.cts.service.AuditLogService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditLogDto auditLogDto;

    @BeforeEach
    void init() {
        // Fix: Ensure all 7 arguments are provided
        // (Id, ServiceName, Operation, RecordId, Timestamp, Details, PerformedBy)
        auditLogDto = new AuditLogDto(1L, "ITEM-SERVICE", "CREATE", 101L, LocalDateTime.now(), "Details", "admin");

        // Mock Security
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("admin");
        when(jwtTokenProvider.getRoles(anyString())).thenReturn(List.of("ROLE_ADMIN", "ROLE_SYSTEM"));
    }

    @Test
    void logEvent() throws Exception {
        when(auditLogService.logEvent(any(AuditLogDto.class))).thenReturn(auditLogDto);

        mockMvc.perform(post("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auditLogDto))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName").value("ITEM-SERVICE"));
    }

    @Test
    void getLogById() throws Exception {
        when(auditLogService.getLogById(1L)).thenReturn(auditLogDto);

        mockMvc.perform(get("/api/logs/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getAllLogs() throws Exception {
        when(auditLogService.getAllLogs()).thenReturn(List.of(auditLogDto));

        mockMvc.perform(get("/api/logs")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].operation").value("CREATE"));
    }

    @Test
    void updateLog() throws Exception {
        when(auditLogService.updateLog(eq(1L), any(AuditLogDto.class))).thenReturn(auditLogDto);

        mockMvc.perform(put("/api/logs/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auditLogDto))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("ITEM-SERVICE"));
    }

    @Test
    void deleteLog() throws Exception {
        SuccessResponse response = new SuccessResponse("AuditLog with ID 1 deleted successfully.");
        when(auditLogService.deleteLog(1L)).thenReturn(response.getMessage());

        mockMvc.perform(delete("/api/logs/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("AuditLog with ID 1 deleted successfully."));
    }
}