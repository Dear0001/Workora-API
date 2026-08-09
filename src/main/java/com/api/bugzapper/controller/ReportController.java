package com.api.bugzapper.controller;

import com.api.bugzapper.model.entity.Report;
import com.api.bugzapper.model.request.ReportPhaseRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.reportService.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/reports")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "report to public phase")
    @PostMapping(value = "/createReportPhase", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createReportPhase(
            @RequestPart("description") String description,
            @RequestPart("problem") String problem,
            @RequestPart("phaseId") String phaseId,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ReportPhaseRequest reportPhaseRequest = ReportPhaseRequest.builder()
                .description(description)
                .problem(problem)
                .phaseId(Integer.parseInt(phaseId))
                .location("Optional")
                .image(image)
                .build();
        reportService.createReportPhase(reportPhaseRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Report phase has been created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view report details")
    @GetMapping("/getReportById/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Integer id) {
        Report report = reportService.getReportById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Report has been found successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(report)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view report details by phase id")
    @GetMapping("/getReportByPhaseId/{phaseId}")
    public ResponseEntity<?> getReportByPhaseId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit,
            @PathVariable Integer phaseId
    ) {
        List<Report> reports = reportService.getReportByPhaseId(offset, limit, phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all report by phase id : " +  phaseId + " successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(reports)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
