package com.api.bugzapper.controller;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.HistoryDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.request.UpdateUserRequest;
import com.api.bugzapper.model.request.UserExperienceRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.userService.AppUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class UserController {
    private final AppUserService appUserService;
    private final GetCurrentUser getCurrentUser;

    public UserController(AppUserService appUserService, GetCurrentUser getCurrentUser) {
        this.appUserService = appUserService;
        this.getCurrentUser = getCurrentUser;
    }

    @PutMapping("/insertUserExperience")
    public ResponseEntity<?> insertUserExperience(@Valid @RequestBody UserExperienceRequest payload){
        ApiResponse<List<Map<String, Object>>> apiResponse = ApiResponse.<List<Map<String, Object>>>builder()
                .payload(appUserService.insertUserExperience(payload))
                .message("User experience inserted successfully")
                .code(200)
                .status(HttpStatus.OK).build();
        return ResponseEntity.ok(apiResponse);
    }
    @PutMapping("/deleteUserExperience")
    public ResponseEntity<?> deleteUserExperience(){
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not authenticated.");
        }
        appUserService.deleteUserExperience(currentUser.getEmail());
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Delete experience successfully.")
                .status(HttpStatus.OK)
                .code(201)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId){
        AppUserDTO appUserDTO = appUserService.getUserById(userId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("User has been updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(appUserDTO)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest){
        AppUserDTO appUserDTO = appUserService.updateUser(updateUserRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("User has been updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(appUserDTO)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteUserByEmail(){
        appUserService.deleteUserByEmail();
        ApiResponse apiResponse = ApiResponse.builder()
                .message("User has been deleted successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/getHistory")
    public ResponseEntity<?> getHistoryByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ){
        List<HistoryDTO> history = appUserService.getAllHistoryByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all history successfully.")
                .status(HttpStatus.OK)
                .payload(history)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/deleteHistoryByRateAndFeedbackId/{id}")
    public ResponseEntity<?> deleteHistoryByRateAndFeedbackId(@PathVariable Integer id){
        appUserService.deleteHistoryByRateAndFeedbackId(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Deleted history successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
