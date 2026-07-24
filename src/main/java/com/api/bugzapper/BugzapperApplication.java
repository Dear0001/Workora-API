package com.api.bugzapper;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.ZoneId;
import java.util.TimeZone;

@EnableAsync
@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Bug Zapper",
        version = "v1",
        description = "Bug Zapper API provides endpoints for managing and tracking bug efficiently. It offers features for user authentication, bug management. The API follows the OpenAPI specification, allowing easy integration with other systems."
))

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class BugzapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugzapperApplication.class, args);
    }
    @PostConstruct
    public void init() {
        // Set default time zone to Cambodia
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Phnom_Penh")));
    }

}
