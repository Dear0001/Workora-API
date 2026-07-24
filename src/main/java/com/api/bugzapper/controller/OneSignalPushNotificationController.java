package com.api.bugzapper.controller;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.repository.UserRoleRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pushNotification")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class OneSignalPushNotificationController {
    private final GetCurrentUser getCurrentUser;
    private final UserRoleRepository userRoleRepository;

    public OneSignalPushNotificationController(GetCurrentUser getCurrentUser, UserRoleRepository userRoleRepository) {
        this.getCurrentUser = getCurrentUser;
        this.userRoleRepository = userRoleRepository;
    }

    @PostMapping("/sendMessageToUser/{userId}/{message}/{title}")
    public ResponseEntity<?> sendMessageToUser(@PathVariable("userId") Integer userId,
                                            @PathVariable("message") String message,
                                            @PathVariable("title") String title)
    {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not authenticated.");
        }
        // Caller may only trigger a push to someone they share a company with —
        // previously any authenticated user could message any other user's device.
        if (!currentUser.getUserId().equals(userId)
                && userRoleRepository.sharesCompanyWith(currentUser.getUserId(), userId) <= 0) {
            throw new CustomNotFoundException("You do not have permission to notify this user.");
        }
        PushNotificationOptions.sendMessageToUser(message, userId, title);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Notification has sent successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
