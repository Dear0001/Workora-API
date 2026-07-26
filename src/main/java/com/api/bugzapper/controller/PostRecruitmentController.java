package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.PostRecruitmentNewsfeed;
import com.api.bugzapper.model.entity.PostRecruitment;
import com.api.bugzapper.model.request.PostRecruitmentRequest;
import com.api.bugzapper.model.request.PostRecruitmentRequestForUpdate;
import com.api.bugzapper.model.request.PostRecruitmentStatusRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.postRecruitmentService.PostRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/postRecruitments")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class PostRecruitmentController {
    private final PostRecruitmentService postRecruitmentService;

    public PostRecruitmentController(PostRecruitmentService postRecruitmentService) {
        this.postRecruitmentService = postRecruitmentService;
    }

    @Operation(summary = "post post recruitment")
    @PostMapping("/createPostRecruitment")
    public ResponseEntity<?> createPostRecruitment(@Valid @RequestBody PostRecruitmentRequest postRecruitmentRequest) {
        PostRecruitment postRecruitment = postRecruitmentService.createPostRecruitment(postRecruitmentRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Create PostRecruitment Successfully")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(postRecruitment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

        @Operation(summary = "view post recruitment details")
    @GetMapping("/getPostRecruitmentById/{id}")
    public ResponseEntity<?> getPostRecruitmentById(@PathVariable Integer id) {
        PostRecruitment postRecruitment = postRecruitmentService.getPostRecruitmentById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get PostRecruitment Successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(postRecruitment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "delete post recruitment")
    @DeleteMapping("/deletePostRecruitmentById/{id}")
    public ResponseEntity<?> deletePostRecruitmentById(@PathVariable Integer id) {
        postRecruitmentService.deletePostRecruitmentById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Delete PostRecruitment Successfully")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all post recruitment (to display on newsfeed)")
    @GetMapping("/getAllPostRecruitments")
    public ResponseEntity<?> getAllPostRecruitment(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<PostRecruitmentNewsfeed> postRecruitment = postRecruitmentService.getAllPostRecruitment(offset, limit);
        ApiResponse<List<PostRecruitmentNewsfeed>> apiResponse = ApiResponse.<List<PostRecruitmentNewsfeed>>builder()
                .message("Get PostRecruitment Successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(postRecruitment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update post recruitment")
    @PutMapping("/updatePostRecruitmentById/{id}")
    public ResponseEntity<?> updatePostRecruitment(@RequestBody PostRecruitmentRequestForUpdate postRecruitmentRequest, @PathVariable Integer id) {
        PostRecruitment postRecruitment = postRecruitmentService.updatePostRecruitmentById(postRecruitmentRequest, id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("PostRecruitment updated Successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(postRecruitment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "close or reopen a recruitment post (owner/recruiter/PM only)")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePostRecruitmentStatus(@Valid @RequestBody PostRecruitmentStatusRequest statusRequest, @PathVariable Integer id) {
        PostRecruitment postRecruitment = postRecruitmentService.updatePostRecruitmentStatus(id, statusRequest.getStatus());
        ApiResponse apiResponse = ApiResponse.builder()
                .message("PostRecruitment status updated Successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(postRecruitment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
