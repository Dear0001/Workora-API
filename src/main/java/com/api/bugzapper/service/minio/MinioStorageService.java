package com.api.bugzapper.service.minio;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Company;
import com.api.bugzapper.repository.AppUserRepository;
import com.api.bugzapper.repository.CompanyRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Object storage via MinIO (S3-compatible API). No AWS S3 — endpoint and keys are MinIO-only.
 */
@Service
@Log4j2
public class MinioStorageService {
    private final AmazonS3 s3client;
    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final GetCurrentUser getCurrentUser;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /** Browser-accessible prefix for returned URLs (must match bucket path on MinIO). */
    @Value("${minio.public-url-base}")
    private String publicUrlBase;

    public MinioStorageService(AmazonS3 s3client, AppUserRepository appUserRepository, CompanyRepository companyRepository, GetCurrentUser getCurrentUser) {
        this.s3client = s3client;
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.getCurrentUser = getCurrentUser;
    }

    public String uploadUserProfile(String keyName, MultipartFile file, String email) throws IOException {
        AppUser appUser = appUserRepository.findUserByEmail(email);
        if (appUser == null) {
            throw new CustomNotFoundException("Email : " + email + " not found");
        }
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(keyName);
        try {
            PutObjectResult putObjectResult = putMultipart(fileName, file);
            if (putObjectResult != null) {
                log.info("File uploaded successfully with metadata: {}", putObjectResult.getMetadata());
                if (appUser.getAvatar() != null && getFile(appUser.getAvatar()) != null) {
                    deleteFile(appUser.getAvatar());
                }
                appUserRepository.insertUserProfile(fileName, email);
                return generateFileUrl(fileName);
            }
            throw new IOException("File upload failed, result is null");
        } catch (AmazonS3Exception e) {
            log.error("MinIO error while uploading file with key: {}", fileName, e);
            throw new IOException("Error occurred while uploading file", e);
        }
    }

    public String uploadCompanyProfile(String keyName, MultipartFile file, Integer id, String type) throws IOException {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Company company = companyRepository.getCompanyById(id);

        if (company == null) {
            throw new CustomNotFoundException("Company : " + id + " not found");
        }

        Integer isAdmin = companyRepository.isOwnerOfCompany(currentUser.getUserId(), id);
        if (isAdmin == null) {
            throw new CustomNotFoundException("User are not the owner of the company");
        }

        String fileName = file.getOriginalFilename();
        assert fileName != null;
        fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(keyName);
        try {
            PutObjectResult putObjectResult = putMultipart(fileName, file);
            if (putObjectResult != null) {
                if ("cover".equals(type)) {
                    if (company.getCoverImage() != null && getFile(company.getCoverImage()) != null) {
                        deleteFile(company.getCoverImage());
                    }
                    companyRepository.insertCompanyCover(fileName, id);
                } else {
                    if (company.getCompanyProfile() != null && getFile(company.getCompanyProfile()) != null) {
                        deleteFile(company.getCompanyProfile());
                    }
                    companyRepository.insertCompanyProfile(fileName, id);
                }
                return generateFileUrl(fileName);
            }
            throw new IOException("File upload failed, result is null");
        } catch (AmazonS3Exception e) {
            log.error("MinIO error while uploading file with key: {}", fileName, e);
            throw new IOException("Error occurred while uploading file", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String uploadFile(String keyName, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(keyName);

        try {
            PutObjectResult putObjectResult = putMultipart(fileName, file);
            if (putObjectResult != null) {
                return generateFileUrl(fileName);
            }
            throw new IOException("File upload failed, result is null");
        } catch (AmazonS3Exception e) {
            log.error("MinIO error while uploading file with key: {}", fileName, e);
            throw new IOException("Error occurred while uploading file", e);
        }
    }

    /**
     * Sets content length (and type when present) so the SDK does not buffer the whole stream in memory.
     */
    private PutObjectResult putMultipart(String objectKey, MultipartFile file) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        long len = file.getSize();
        if (len >= 0) {
            metadata.setContentLength(len);
        }
        String ct = file.getContentType();
        if (ct != null && !ct.isBlank()) {
            metadata.setContentType(ct);
        }
        return s3client.putObject(bucketName, objectKey, file.getInputStream(), metadata);
    }

    public S3Object getFile(String keyName) {
        try {
            S3Object s3Object = s3client.getObject(bucketName, keyName);
            if (s3Object != null) {
                log.info("Successfully retrieved file with key: {}", keyName);
                return s3Object;
            }
            log.warn("Object is null. File might not exist with key: {}", keyName);
            return null;
        } catch (AmazonS3Exception e) {
            log.error("Error occurred while retrieving file with key: {}", keyName, e);
            return null;
        }
    }

    public void deleteFile(String keyName) {
        try {
            getFile(keyName);
            s3client.deleteObject(bucketName, keyName);
            log.info("Successfully deleted file with key: {}", keyName);
        } catch (AmazonS3Exception e) {
            log.error("Error occurred while deleting file with key: {}", keyName, e);
        }
    }

    private String generateFileUrl(String keyName) {
        String base = publicUrlBase.endsWith("/") ? publicUrlBase : publicUrlBase + "/";
        return base + keyName;
    }
}
