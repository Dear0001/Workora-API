package com.api.bugzapper.service.userService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.HistoryDTO;
import com.api.bugzapper.model.dto.OtpsDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.request.*;
import com.api.bugzapper.repository.AppUserRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.otpService.OtpService;
import com.api.bugzapper.service.taskService.TaskService;
import com.api.bugzapper.util.EmailUtil;
import com.api.bugzapper.util.OtpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AppUserService {
    private final OtpUtil otpUtil;
    private final EmailUtil emailUtil;

    private final AppUserRepository appUserRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TaskService taskService;
    private final UserRoleRepository userRoleRepository;
    private final GetCurrentUser getCurrentUser;

    public AppUserService(OtpUtil otpUtil, EmailUtil emailUtil, AppUserRepository appUserRepository, OtpService otpService, PasswordEncoder passwordEncoder, ModelMapper modelMapper, @Lazy TaskService taskService, UserRoleRepository userRoleRepository, GetCurrentUser getCurrentUser) {
        this.otpUtil = otpUtil;
        this.emailUtil = emailUtil;
        this.appUserRepository = appUserRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.taskService = taskService;
        this.userRoleRepository = userRoleRepository;
        this.getCurrentUser = getCurrentUser;
    }
    public AppUserDTO registerUserOnLocal(AppUserRequest appUserRequest) {

        // ===============================
        // 1. NULL SAFETY CHECKS
        // ===============================
        if (appUserRequest == null) {
            throw new CustomNotFoundException("Request cannot be null");
        }

        if (appUserRequest.getEmail() == null || appUserRequest.getEmail().isBlank()) {
            throw new CustomNotFoundException("Email cannot be null or empty");
        }

        if (appUserRequest.getPassword() == null || appUserRequest.getPassword().isBlank()) {
            throw new CustomNotFoundException("Password cannot be null or empty");
        }

        if (appUserRequest.getConfirmPassword() == null || appUserRequest.getConfirmPassword().isBlank()) {
            throw new CustomNotFoundException("Confirm password cannot be null or empty");
        }

        // ===============================
        // 2. NORMALIZE EMAIL
        // ===============================
        String email = appUserRequest.getEmail().toLowerCase();
        appUserRequest.setEmail(email);

        // ===============================
        // 3. PASSWORD MATCH CHECK
        // ===============================
        if (!appUserRequest.getPassword().equals(appUserRequest.getConfirmPassword())) {
            throw new CustomNotFoundException("Password and confirm password do not match");
        }

        // ===============================
        // 4. CHECK DUPLICATE USER
        // ===============================
        if (appUserRepository.findUserByEmail(email) != null) {
            throw new CustomNotFoundException("This email already exists");
        }

        // ===============================
        // 5. ENCODE PASSWORD
        // ===============================
        String encodedPassword = passwordEncoder.encode(appUserRequest.getPassword());
        appUserRequest.setPassword(encodedPassword);

        // ===============================
        // 6. SAVE USER
        // ===============================
        AppUser appUser = appUserRepository.saveLocalUser(appUserRequest);

        if (appUser == null) {
            throw new RuntimeException("Failed to create user");
        }

        // ===============================
        // 7. GENERATE OTP
        // ===============================
        OtpsDTO otpsDTO = otpUtil.generateOTP(appUser.getUserId());

        if (otpsDTO == null) {
            throw new RuntimeException("Failed to generate OTP");
        }

        otpService.saveOtp(otpsDTO);

        // ===============================
        // 8. SEND EMAIL (SAFE)
        // ===============================
        try {
            emailUtil.sendOtpEmail(appUser.getEmail(), otpsDTO.getOptCode());
        } catch (MessagingException e) {
            // user already created → do NOT rollback DB silently
            throw new RuntimeException("User created but OTP email failed to send");
        }

        // ===============================
        // 9. RETURN DTO
        // ===============================
        return modelMapper.map(appUser, AppUserDTO.class);
    }
    public AppUserDTO registerUserWithGoogle(GoogleRequest googleRequest) {
        // Check if the email is already exist
        if (appUserRepository.findUserByEmail(googleRequest.getEmail()) != null) {
            throw new CustomNotFoundException("This email already exists");
        }

        AppUser appUser = appUserRepository.saveUserWithGoogle(googleRequest);
        System.out.println(appUser.getUserId());

        return modelMapper.map(appUser, AppUserDTO.class);
    }
    public AppUserDTO updateUser(UpdateUserRequest updateUserRequest) {
        String email = getCurrentUser.getCurrentUser().getEmail();
        findUserByEmail(email);

        AppUser appUser = appUserRepository.updateUser(updateUserRequest, email.toLowerCase());
        return modelMapper.map(appUser, AppUserDTO.class);
    }
    public AppUserDTO getUserById(Integer id) {
        AppUserDTO appUserDTO = appUserRepository.getUserById(id);
        if (appUserDTO == null) {
            throw new CustomNotFoundException("User with id : " + id + " not found");
        }
        return appUserDTO;
    }
    public void deleteUserByEmail() {
        String email = getCurrentUser.getCurrentUser().getEmail();
        findUserByEmail(email.toLowerCase());
        appUserRepository.deleteUserByEmail(email.toLowerCase());
    }
    public List<HistoryDTO> getAllHistoryByUserId(Integer offset, Integer limit) {
        AppUser appUser = getCurrentUser.getCurrentUser();
        getUserById(appUser.getUserId());
        return appUserRepository.getAllHistoryByUserId(offset, limit, appUser.getUserId());
    }
    public AppUserDTO getUserDtoById(Integer userId) {
        AppUserDTO appUserDTO = appUserRepository.getUserDtoById(userId);
        if (appUserDTO == null) {
            throw new CustomNotFoundException("User with id : " + userId + " not found");
        }
        return appUserDTO;
    }
    public Integer getUserIdByTaskId(Integer taskId) {
        taskService.getTaskById(taskId);
        return appUserRepository.getUserIdByTaskId(taskId);
    }
    public AppUser findUserByEmailForGoogleAuth(String email) {
        return appUserRepository.findUserByEmail(email.toLowerCase());
    }
    public void setUserRole(Integer userId, Integer roleId) {
        userRoleRepository.setUserRole(userId, roleId);
    }
    public String getAvatarByUserId(Integer userId) {
        getUserById(userId);
        return appUserRepository.getAvatarByUserId(userId);
    }
    public void deleteHistoryByRateAndFeedbackId(Integer id) {
        appUserRepository.deleteHistoryByRateAndFeedbackId(id);
    }
    public void deleteUserExperience(String email) {
        appUserRepository.findUserByEmail(email);
        appUserRepository.deleteUserExperience(email);
    }
    public String verifyUser(String otp) {
        if (otp == null || otp.isBlank()) {
            return "Invalid OTP code. Please check and try again.";
        }
        OtpsDTO otpsDTO = otpService.getOtp(otp);
        if (otpsDTO == null) {
            return "Invalid OTP code. Please check and try again.";
        }

        if (otpsDTO.isVerify()) {
            return "OTP has already been used";
        }

        LocalDateTime now = LocalDateTime.now();

        if (otpsDTO.getExpiration() != null && now.isAfter(otpsDTO.getExpiration())) {
            return "OTP is expired";
        }

        otpsDTO.setVerify(true);
        otpService.updateOtp(otpsDTO);

        AppUser appUser = appUserRepository.findById(otpsDTO.getUserId());
        if (appUser == null) {
            return "Invalid OTP code. User not found.";
        }
        appUserRepository.setIsVerifiedToTrue(appUser.getEmail().toLowerCase());

        return "OTP is verified";
    }
    public String resendOtp(String email) {
        AppUser appUser = appUserRepository.findUserByEmail(email.toLowerCase());
        if (appUser == null) {
            throw new CustomNotFoundException("Email : " + email + " not found");
        }

        OtpsDTO otpsDTO = otpUtil.generateOTP(appUser.getUserId());
        otpService.saveOtp(otpsDTO);

        try {
            emailUtil.sendOtpEmail(appUser.getEmail(), otpsDTO.getOptCode());
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }

        return "OTP has been sent to your email";
    }
    public AppUserDTO findUserByEmail(String email) {
        AppUser appUser = appUserRepository.findUserByEmail(email);
        if (appUser == null) {
            throw new CustomNotFoundException("Email : " + email + " not found");
        }
        return modelMapper.map(appUser, AppUserDTO.class);
    }
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findUserByEmail(email);
    }
    public void resetPasswordByEmail(ForgetPasswordRequest forgetPasswordRequest, String email, String otpCode) {
        // Check if the confirm-password does not match the given password
        if (!forgetPasswordRequest.getPassword().equals(forgetPasswordRequest.getConfirmPassword())) {
            throw new CustomNotFoundException("Password is not matched");
        }

        // Retrieve the user's information
        if(appUserRepository.findUserByEmail(email) == null){
            throw new CustomNotFoundException("Email : " + email + " not found");
        }

        OtpsDTO otpsDTO = otpService.getOtp(otpCode);
        if (otpsDTO == null) {
            throw new CustomNotFoundException("Invalid OTP code. Please try again.");
        }
        if (!otpsDTO.isVerify()) {
            throw new CustomNotFoundException("OTP is not verified");
        }

        // Encrypt the new password before updating
        forgetPasswordRequest.setPassword(passwordEncoder.encode(forgetPasswordRequest.getPassword()));

        // Update the password
        appUserRepository.forgetPassword(forgetPasswordRequest,email);
    }
    public void updatePassword(PasswordRequest passwordRequest, String email) {
        if (!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new CustomNotFoundException("Password is not matched");
        }

        // Retrieve the user's information
        AppUser appUser = appUserRepository.findUserByEmail(email);
        if(appUser == null){
            throw new CustomNotFoundException("Email : " + email + " not found");
        }

        // Use the PasswordEncoder to check if the provided current password matches the encrypted password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), appUser.getPassword())) {
            throw new CustomNotFoundException("Current password not match");
        }

        // Encrypt the new password before updating
        passwordRequest.setPassword(passwordEncoder.encode(passwordRequest.getPassword()));

        // Update the password
        appUserRepository.resetPasswordByEmail(passwordRequest, email);
    }
    public List<Map<String, Object>> insertUserExperience(UserExperienceRequest userExperienceRequest) {
        String email = getCurrentUser.getCurrentUser().getEmail().toString();
        AppUser user = appUserRepository.findUserByEmail(email);
        if (user == null) {
            throw new CustomNotFoundException("Email : " + email + " not found");
        }

        String experienceJson;
        try {
            experienceJson = new ObjectMapper().writeValueAsString(userExperienceRequest.getExperience());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        appUserRepository.insertUserExperience(experienceJson, email);
        user.setExperience(userExperienceRequest.getExperience());
        return user.getExperience();
    }

}
