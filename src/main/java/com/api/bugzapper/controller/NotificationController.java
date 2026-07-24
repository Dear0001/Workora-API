package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.NotificationDTO;
import com.api.bugzapper.model.entity.Notification;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.notificationService.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.ibatis.annotations.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})

public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Update notification to read")
    @PutMapping("/updateNotificationToRead/{id}")
    public ResponseEntity<?> updateNotificationToRead(@PathVariable Integer id) {
        notificationService.updateNotificationToRead(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Update notification successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification by id")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Notification deleted successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Count unread notifications for the current user")
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnreadNotification() {
        Integer count = notificationService.countUnreadNotification();
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Unread notification count retrieved successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(count)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
