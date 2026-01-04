package com.cts.service;

import com.cts.client.AuditServiceClient;
import com.cts.dtos.AuditLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditServiceClient auditServiceClient;

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
            auditLog.setServiceName("item-service");
            auditLog.setOperation(operation);
            auditLog.setRecordId(recordId);
            auditLog.setDetails(details);
            auditLog.setPerformedBy(currentUser);
            // Send
            auditServiceClient.logEvent(auditLog);
        } catch (Exception e) {
            System.err.println("Error logging event to audit service: " + e.getMessage());
        }
    }
}
