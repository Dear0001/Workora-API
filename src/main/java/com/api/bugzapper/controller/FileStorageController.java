package com.api.bugzapper.controller;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.minio.MinioStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

/**
 * File upload/download backed by MinIO (not AWS S3). Base path unchanged: /api/v1/files
 */
@RestController
@RequestMapping("/api/v1/files")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "https://workora-delta.vercel.app"})
public class FileStorageController {
    private final MinioStorageService minioStorageService;
    private final GetCurrentUser getCurrentUser;
    private final UserRoleRepository userRoleRepository;

    public FileStorageController(MinioStorageService minioStorageService, GetCurrentUser getCurrentUser, UserRoleRepository userRoleRepository) {
        this.minioStorageService = minioStorageService;
        this.getCurrentUser = getCurrentUser;
        this.userRoleRepository = userRoleRepository;
    }

    @PostMapping(path = "/uploadUserProfile/{email}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadUserFile(@RequestParam("file") MultipartFile file, @PathVariable String email) throws IOException {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null || !currentUser.getEmail().equalsIgnoreCase(email)) {
            throw new CustomNotFoundException("You can only upload your own profile picture.");
        }
        String fileUrl = minioStorageService.uploadUserProfile(file.getOriginalFilename(), file, email);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(201)
                .message("Successfully uploaded file")
                .status(HttpStatus.CREATED)
                .payload(fileName)
                .fileUrl(fileUrl)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping(path = "/uploadFile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = minioStorageService.uploadFile(file.getOriginalFilename(), file);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(201)
                .message("Successfully uploaded file")
                .status(HttpStatus.CREATED)
                .payload(fileName)
                .fileUrl(fileUrl)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping(path = "/uploadCompanyProfile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadCompanyProfile(@RequestParam("file") MultipartFile file, @RequestParam Integer id, @RequestParam String type) throws IOException {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null || userRoleRepository.isOwnerOrRecruiter(currentUser.getUserId(), id) <= 0) {
            throw new CustomNotFoundException("You do not have permission to update this company's images.");
        }
        String fileUrl = minioStorageService.uploadCompanyProfile(file.getOriginalFilename(), file, id, type);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(201)
                .message("Successfully uploaded file")
                .status(HttpStatus.CREATED)
                .payload(fileName)
                .fileUrl(fileUrl)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        var s3Object = minioStorageService.getFile(fileName);
        if (s3Object == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<InputStreamResource> viewFile(@PathVariable String fileName) {
        var s3Object = minioStorageService.getFile(fileName);
        if (s3Object == null) {
            return ResponseEntity.notFound().build();
        }
        var content = s3Object.getObjectContent();
        MediaType mediaType = guessMediaType(fileName);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(content));
    }

    private static MediaType guessMediaType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        minioStorageService.deleteFile(filename);
        return ResponseEntity.ok("Delete");
    }
}
