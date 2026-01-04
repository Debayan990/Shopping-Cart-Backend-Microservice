package com.cts.controllers;

import com.cts.dtos.NotificationDto;
import com.cts.dtos.SuccessResponse;
import com.cts.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<NotificationDto> sendNotification(@Valid @RequestBody NotificationDto notificationDto) {
        return new ResponseEntity<>(notificationService.sendNotification(notificationDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<NotificationDto> updateNotification(@PathVariable Long id, @Valid @RequestBody NotificationDto notificationDto) {
        return ResponseEntity.ok(notificationService.updateNotification(id, notificationDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> deleteNotification(@PathVariable Long id) {
        SuccessResponse msg = new SuccessResponse(notificationService.deleteNotification(id));
        return ResponseEntity.ok(msg);
    }
}