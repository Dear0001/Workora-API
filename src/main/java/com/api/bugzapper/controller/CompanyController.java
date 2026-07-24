package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.CompanyMemberDTO;
import com.api.bugzapper.model.dto.CompanyRatingDTO;
import com.api.bugzapper.model.dto.TopCompany;
import com.api.bugzapper.model.entity.Company;
import com.api.bugzapper.model.entity.UserRole;
import com.api.bugzapper.model.request.CompanyRequest;
import com.api.bugzapper.model.request.InviteMemberRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.model.response.CompanyMemberResponse;
import com.api.bugzapper.service.companyService.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "create company")
    @PostMapping("/createCompany")
    public ResponseEntity<?> create(@Valid @RequestBody CompanyRequest companyRequest) {
        Company company = companyService.createCompany(companyRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Company has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(company)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view company details")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        Company company = companyService.getCompanyById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Company with id " + id + " was founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(company)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "company profile")
    @GetMapping("/getCompanyByIdWithRating/{id}")
    public ResponseEntity<?> getCompanyByIdWithRating(@PathVariable("id") Integer companyId) {
        CompanyRatingDTO company = companyService.getCompanyByIdWithRating(companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Company with id " + companyId + " was founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(company)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update company")
    @PutMapping("{id}")
    public ResponseEntity<?> updateCompany(@Positive @PathVariable Integer id,@Valid @RequestBody CompanyRequest companyRequest) {
        Company company = companyService.updateCompany(id, companyRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Company with id " + id + "has updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(company)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "delete company")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Integer id) {
        companyService.deleteCompany(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Company wit id " + id + " has deleted successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "join company by code")

    @PostMapping("/join-company")
    public ResponseEntity<?> joinCompany(@RequestParam String code) {
        companyService.addUserToCompanyByCode(code);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Successfully joined the company.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all top company")
    @GetMapping("/getTopCompany")
    public ResponseEntity<?> getTopCompany() {
        List<TopCompany> companies = companyService.getTopCompany();
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all member successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);

    }

    @Operation(summary = "get current user's role in company")
    @GetMapping("/{companyId}/current-user-role")
    public ResponseEntity<?> getCurrentUserRoleInCompany(@PathVariable Integer companyId) {
        String roleName = companyService.getCurrentUserRoleNameInCompany(companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Current user role retrieved successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(roleName)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all member of company")
    @GetMapping("/members/{companyId}")
    public ResponseEntity<?> companyMembers(@PathVariable Integer companyId) {
        List<CompanyMemberDTO> companyMemberDTOS = companyService.companyMembers(companyId);
        CompanyMemberResponse apiResponse = CompanyMemberResponse.builder()
                .message("Get all member successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .totalMembers(companyMemberDTOS.size())
                .payload(companyMemberDTOS)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "change member role in company")
    @PutMapping("/switch-user-role")
    public ResponseEntity<?> switchUserRole(@RequestParam Integer userId, @RequestParam Integer roleId, @RequestParam Integer companyId) {
        UserRole userRole = companyService.updateUserRole(userId, roleId, companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("UserRole for user with id " + userId + " has been updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(userRole)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "invite an existing user into the company with a chosen role (owner only)")
    @PostMapping("/invite-member")
    public ResponseEntity<?> inviteMember(@Valid @RequestBody InviteMemberRequest inviteMemberRequest) {
        UserRole userRole = companyService.inviteMember(inviteMemberRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Member invited successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(userRole)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "remove member from company")
    @DeleteMapping("/remove/{userId}/from/{companyId}")
    public ResponseEntity<?> removeUserFromCompany(@PathVariable Integer userId, @PathVariable Integer companyId) {
        companyService.removeUserFromCompany(userId, companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("User with id " + userId + " has been removed from company with id " + companyId + " successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
