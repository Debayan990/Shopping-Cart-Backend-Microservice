package com.cts.service;

import com.cts.dtos.AuditLogDto;
import com.cts.entities.AuditLog;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuditLogServiceImplTest {

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    private AuditLog auditLog;
    private AuditLogDto auditLogDto;

    @BeforeEach
    void init() {
        // Fix: Ensure all 7 arguments are provided
        // (Id, ServiceName, Operation, RecordId, Timestamp, Details, PerformedBy)
        auditLog = new AuditLog(1L, "ITEM-SERVICE", "CREATE", 101L, LocalDateTime.now(), "Created Item", "admin");
        auditLogDto = new AuditLogDto(1L, "ITEM-SERVICE", "CREATE", 101L, auditLog.getTimestamp(), "Created Item", "admin");
    }

    @Test
    void logEvent() {
        when(modelMapper.map(any(AuditLogDto.class), eq(AuditLog.class))).thenReturn(auditLog);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        when(modelMapper.map(any(AuditLog.class), eq(AuditLogDto.class))).thenReturn(auditLogDto);

        AuditLogDto result = auditLogService.logEvent(auditLogDto);

        assertNotNull(result);
        assertEquals("ITEM-SERVICE", result.getServiceName());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getLogById() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(modelMapper.map(any(AuditLog.class), eq(AuditLogDto.class))).thenReturn(auditLogDto);

        AuditLogDto result = auditLogService.getLogById(1L);

        assertNotNull(result);
        assertEquals(101L, result.getRecordId());
        verify(auditLogRepository).findById(1L);
    }

    @Test
    void getLogById_NotFound() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditLogService.getLogById(99L));
        verify(auditLogRepository).findById(99L);
    }

    @Test
    void getAllLogs() {
        when(auditLogRepository.findAll()).thenReturn(List.of(auditLog));
        when(modelMapper.map(any(AuditLog.class), eq(AuditLogDto.class))).thenReturn(auditLogDto);

        List<AuditLogDto> results = auditLogService.getAllLogs();

        assertEquals(1, results.size());
        verify(auditLogRepository).findAll();
    }

    @Test
    void updateLog() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        when(modelMapper.map(any(AuditLog.class), eq(AuditLogDto.class))).thenReturn(auditLogDto);

        AuditLogDto result = auditLogService.updateLog(1L, auditLogDto);

        assertNotNull(result);
        assertEquals("CREATE", result.getOperation());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void deleteLog() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        doNothing().when(auditLogRepository).delete(any(AuditLog.class));

        String result = auditLogService.deleteLog(1L);

        assertEquals("AuditLog with ID 1 deleted successfully.", result);
        verify(auditLogRepository).delete(auditLog);
    }
}