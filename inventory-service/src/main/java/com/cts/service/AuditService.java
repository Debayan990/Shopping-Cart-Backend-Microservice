package com.cts.service;

import com.cts.client.AuditServiceClient;
import com.cts.dtos.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditServiceClient auditServiceClient;    // Inject the Feign Client

    public void logEvent(String operation, Long recordId, String details) {
        try {
            // Get the current user from Security Context
            String currentUser = "UNKNOWN";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                currentUser = auth.getName(); // Returns "admin", "system-cart-service", etc.
            }

            // Building DTO
            AuditLogDto auditLog = new AuditLogDto();
            auditLog.setServiceName("inventory-service");
            auditLog.setOperation(operation);
            auditLog.setRecordId(recordId);
            auditLog.setDetails(details);
            auditLog.setPerformedBy(currentUser);
            // Send
            auditServiceClient.logEvent(auditLog);
        } catch (Exception e) {
            log.error("Failed to log event to Audit Service. Error: {}", e.getMessage());
        }
    }
}
