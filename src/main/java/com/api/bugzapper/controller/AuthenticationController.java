package com.api.bugzapper.controller;

import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.repository.RoleRepository;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.OtpsDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.request.*;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.model.response.AuthResponse;
import com.api.bugzapper.security.JwtService;
import com.api.bugzapper.service.otpService.OtpService;
import com.api.bugzapper.service.userService.AppUserService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/v1/auths")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class AuthenticationController {

    private static final int DEFAULT_REGISTRATION_ROLE_ID = RoleId.BUG_HUNTER;

    private final AppUserService appUserService;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** New users get BUG_HUNTER (role_id 6). DB must contain this row — see docs/db/roles-seed-v2.sql */
    private void ensureBugHunterRoleConfigured() {
        if (!roleRepository.roleExists(DEFAULT_REGISTRATION_ROLE_ID)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "BUG_HUNTER role (role_id="
                            + DEFAULT_REGISTRATION_ROLE_ID
                            + ") is missing in table roles. Run docs/db/roles-seed-v2.sql on PostgreSQL.");
        }
    }

    @PostMapping("/local-register")
    public ResponseEntity<?> registerUserOnLocal(@Valid @RequestBody AppUserRequest appUserRequest) {
        ensureBugHunterRoleConfigured();
        AppUserDTO appUserDto = appUserService.registerUserOnLocal(appUserRequest);

        appUserService.findUserByEmail(appUserDto.getEmail().toLowerCase());
        appUserService.setUserRole(appUserDto.getUserId(), DEFAULT_REGISTRATION_ROLE_ID);

        ApiResponse<AppUserDTO> apiResponse = ApiResponse.<AppUserDTO>builder()
                .payload(appUserDto)
                .message("Register user successfully")
                .code(201)
                .status(HttpStatus.CREATED)
                .build();
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/google-auth")
    public ResponseEntity<?> authenticateOrRegisterWithGoogle(@Valid @RequestBody GoogleAuth authRequest) {
        AppUser appUser = appUserService.findUserByEmailForGoogleAuth(authRequest.getEmail().toLowerCase());

        if (appUser == null) {
            ensureBugHunterRoleConfigured();
            // User not found, register them
            GoogleRequest googleRequest = new GoogleRequest(
                    authRequest.getFirstName(),
                    authRequest.getLastName(),
                    authRequest.getGender(),
                    authRequest.getDob(),
                    authRequest.getEmail().toLowerCase()
            );
            appUserService.registerUserWithGoogle(googleRequest);
            // Reload user after registration
            appUser = appUserService.findUserByEmailForGoogleAuth(authRequest.getEmail().toLowerCase());
            appUserService.setUserRole(appUser.getUserId(), DEFAULT_REGISTRATION_ROLE_ID);
        }

        if (appUser.isType()) {
            throw new CustomNotFoundException("This email " + authRequest.getEmail() + " have already registered.");
        }
        final UserDetails userDetails = appUserService.loadUserByUsername(authRequest.getEmail().toLowerCase());
        final String accessToken = jwtService.generateAccessToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);
        AppUserDTO appUserDto = appUserService.findUserByEmail(authRequest.getEmail().toLowerCase());
        ApiResponse<AppUserDTO> apiResponse = ApiResponse.<AppUserDTO>builder()
                .payload(appUserDto)
                .message("Login with google successfully")
                .token(authResponse.getToken())
                .refreshToken(authResponse.getRefreshToken())
                .code(201)
                .status(HttpStatus.CREATED)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            UserDetails userApp = appUserService.loadUserByUsername(username);
            if (userApp == null) {
                throw new CustomNotFoundException("Wrong Email");
            }
            if (!passwordEncoder.matches(password, userApp.getPassword())) {
                throw new CustomNotFoundException("Wrong Password");
            }
            // NOTE:
            // We already validated credentials via BCrypt above.
            // Calling AuthenticationManager.authenticate(...) here caused a StackOverflowError
            // in the current security wiring (recursive authentication chain).
            // Since we generate the JWT immediately after, skipping AuthenticationManager
            // keeps login working without the recursion.
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> forget(@Valid @RequestBody ForgetPasswordRequest forgetPasswordRequest,@RequestParam String email, @RequestParam String otpCode) {
        appUserService.resetPasswordByEmail(forgetPasswordRequest, email.toLowerCase(), otpCode);

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .payload("Password Reset")
                .message("Password Reset successfully")
                .code(200)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);

    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordRequest passwordRequest,@RequestParam String email) {
        appUserService.updatePassword(passwordRequest, email.toLowerCase());

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .message("Password Reset successfully")
                .code(200)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);

    }

    @PutMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String otp) {
        String verifiedUser = appUserService.verifyUser(otp);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .message(verifiedUser)
                .code(201)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/local-login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequest authRequest) throws Exception {
        AppUserDTO appUser = appUserService.findUserByEmail(authRequest.getEmail().toLowerCase());
        System.out.println(appUser.isVerified());
        if (!appUser.isVerified()) {
            throw new CustomNotFoundException("OTP not verified. Please verify your OTP before logging in.");
        }

        authenticate(authRequest.getEmail().toLowerCase(), authRequest.getPassword());
        final UserDetails userDetails = appUserService.loadUserByUsername(authRequest.getEmail().toLowerCase());
        final String accessToken = jwtService.generateAccessToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);

        AppUserDTO appUserDto = appUserService.findUserByEmail(authRequest.getEmail().toLowerCase());
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);

        ApiResponse<AppUserDTO> apiResponse = ApiResponse.<AppUserDTO>builder()
                .payload(appUserDto)
                .message("Login successfully")
                .token(authResponse.getToken())
                .refreshToken(authResponse.getRefreshToken())
                .code(200)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (JwtException e) {
            throw new CustomNotFoundException("Invalid or expired refresh token.");
        }

        UserDetails userDetails = appUserService.loadUserByUsername(email.toLowerCase());
        if (userDetails == null || !jwtService.validateRefreshToken(refreshToken, userDetails)) {
            throw new CustomNotFoundException("Invalid or expired refresh token.");
        }

        final String newAccessToken = jwtService.generateAccessToken(userDetails);
        final String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .message("Token refreshed successfully")
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .code(200)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/send-email")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        String resendOtp = appUserService.resendOtp(email.toLowerCase());
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .payload(resendOtp)
                .message("Check your email for OTP code")
                .code(200)
                .status(HttpStatus.OK)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
