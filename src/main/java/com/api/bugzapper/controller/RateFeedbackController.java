package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.RateFeedbackFromCompanyToUser;
import com.api.bugzapper.model.entity.RateFeedback;
import com.api.bugzapper.model.entity.RateFeedbackFromUser;
import com.api.bugzapper.model.entity.RateFeedbackToCompany;
import com.api.bugzapper.model.request.RateFeedbackCompanyToUserRequest;
import com.api.bugzapper.model.request.RateFeedbackUserToCompanyRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.RateFeedbackService.RateFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rateFeedback")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class RateFeedbackController {
    private final RateFeedbackService rateFeedbackService;

    public RateFeedbackController(RateFeedbackService rateFeedbackService) {
        this.rateFeedbackService = rateFeedbackService;
    }

    @PostMapping("/rateAndFeedbackUser")
    @Operation(summary = "Rate and feedback from user to company.")
    public ResponseEntity<?> userRateFeedbackToCompany(@Valid @RequestBody RateFeedbackUserToCompanyRequest request) {
        RateFeedback rateFeedback = rateFeedbackService.userRateFeedbackToCompany(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("rate and feedback has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(rateFeedback)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/rateAndFeedbackCompany")
    @Operation(summary = "Rate and feedback from company to user.")
    public ResponseEntity<?> companyRateFeedbackToUser(@Valid @RequestBody RateFeedbackCompanyToUserRequest request) {
        RateFeedback rateFeedback = rateFeedbackService.companyRateFeedbackToUser(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("rate and feedback has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(rateFeedback)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view rate and feedback details")
    @GetMapping("/getRateAndFeedbackById/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        RateFeedback rateFeedback = rateFeedbackService.findById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("rate and feedback with id " + id + " has founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(rateFeedback)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get All rate and feedback of a user")
    @GetMapping("/getAllRateFeedbackOfUser/{userId}")
    public ResponseEntity<?> getRateAndFeedbackByUserId(@PathVariable Integer userId) {
        RateFeedbackToCompany companyRateFeedbacks = rateFeedbackService.getRateAndFeedbackByUserId(userId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all rate and feedback of user successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companyRateFeedbacks)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get All rate and feedback of a company to user")
    @GetMapping("/getAllRateFeedbackOfCompanyToUser/{userId}")
    public ResponseEntity<?> getAllRateFeedbackOfCompanyToUser(@PathVariable Integer userId) {
        List<RateFeedbackFromCompanyToUser> companyRateFeedbacks = rateFeedbackService.getAllRateFeedbackOfCompanyToUser(userId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all rate and feedback of company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companyRateFeedbacks)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get All rate and feedback of a company")
    @GetMapping("/getAllRateFeedbackOfCompany/{companyId}")
    public ResponseEntity<?> getAllRateFeedbackOfCompany(@PathVariable Integer companyId) {
        List<RateFeedbackFromUser> companyRateFeedbacks = rateFeedbackService.getAllRateFeedbackOfCompany(companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all rate and feedback of company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companyRateFeedbacks)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
