package com.api.bugzapper.configuration;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures the configured bucket exists (MinIO or S3-compatible).
 */
@Configuration
@Log4j2
public class MinioBucketInitializer {

    @Bean
    ApplicationRunner ensureMinioBucket(AmazonS3 amazonS3, @Value("${minio.bucket-name}") String bucket) {
        return args -> {
            try {
                if (!amazonS3.doesBucketExistV2(bucket)) {
                    amazonS3.createBucket(bucket);
                    log.info("Created MinIO bucket: {}", bucket);
                }
            } catch (Exception e) {
                log.error("Could not verify or create bucket '{}'. Check minio.url and credentials.", bucket, e);
            }
        };
    }
}
