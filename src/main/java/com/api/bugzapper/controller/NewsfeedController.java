package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.PhaseNewsfeedDTO;
import com.api.bugzapper.model.dto.PostRecruitmentNewsfeed;
import com.api.bugzapper.model.entity.PostRecruitment;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.phaseService.PhaseService;
import com.api.bugzapper.service.postRecruitmentService.PostRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/newsfeed")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "https://www.bugzapper.dev"})
public class NewsfeedController {
    private final PostRecruitmentService postRecruitmentService;
    private final PhaseService phaseService;
    private final CompanyService companyService;

    public NewsfeedController(PostRecruitmentService postRecruitmentService, PhaseService phaseService, CompanyService companyService) {
        this.postRecruitmentService = postRecruitmentService;
        this.phaseService = phaseService;
        this.companyService = companyService;
    }

    @Operation(summary = "get all post to display in newsfeed")
    @GetMapping("/getNewsfeed")
    public ResponseEntity<?> getNewsfeed(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<PostRecruitmentNewsfeed> postRecruitments = postRecruitmentService.getAllPostRecruitment(offset, limit);
        List<PhaseNewsfeedDTO> phases = phaseService.getAllPublicPhase(offset, limit);

        List<Object> newsfeedDTO = new ArrayList<>();
        newsfeedDTO.addAll(postRecruitments);
        newsfeedDTO.addAll(phases);

        Set<Object> uniqueItems = new HashSet<>();
        List<Object> newFeedItems = new ArrayList<>();
        Random random = new Random();

        while (uniqueItems.size() < newsfeedDTO.size()) {
            int index = random.nextInt(newsfeedDTO.size());
            Object item = newsfeedDTO.get(index);
            if (uniqueItems.add(item)) {
                newFeedItems.add(item);
            }
        }

        newFeedItems.sort((a, b) -> {
            LocalDateTime createdAtA = getCreatedAt(a);
            LocalDateTime createdAtB = getCreatedAt(b);
            return createdAtB.compareTo(createdAtA);
        });

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all newsfeed successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(newFeedItems)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    private LocalDateTime getCreatedAt(Object obj) {
        if (obj instanceof PostRecruitmentNewsfeed) {
            PostRecruitmentNewsfeed post = (PostRecruitmentNewsfeed) obj;
            return post.getCreatedAt();
        } else if (obj instanceof PhaseNewsfeedDTO) {
            PhaseNewsfeedDTO phase = (PhaseNewsfeedDTO) obj;
            return phase.getCreatedAt();
        }
        return LocalDateTime.MIN; // Default value if type is unknown
    }

    @Operation(summary = "get all posts to display in newsfeed by company id")
    @GetMapping("/getNewsfeedByCompany")
    public ResponseEntity<?> getNewsfeedByCompany(
            @RequestParam Integer companyId,
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        companyService.getCompanyById(companyId);

        List<PostRecruitmentNewsfeed> postRecruitments = postRecruitmentService.getAllPostRecruitmentByCompanyId(companyId, offset, limit);
        System.out.println(postRecruitments + "all newsfeed");
        List<PhaseNewsfeedDTO> phases = phaseService.getAllPublicPhasesByCompanyId(companyId, offset, limit);

        List<Object> newsfeedDTO = new ArrayList<>();
        newsfeedDTO.addAll(postRecruitments);
        newsfeedDTO.addAll(phases);

        Set<Object> uniqueItems = new HashSet<>();
        List<Object> newFeedItems = new ArrayList<>();
        Random random = new Random();

        while (uniqueItems.size() < newsfeedDTO.size()) {
            int index = random.nextInt(newsfeedDTO.size());
            Object item = newsfeedDTO.get(index);
            if (uniqueItems.add(item)) {
                newFeedItems.add(item);
            }
        }

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all newsfeed by company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(newFeedItems)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
