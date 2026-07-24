package com.api.bugzapper.service.RateFeedbackService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.RateFeedbackFromCompanyToUser;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.RateFeedback;
import com.api.bugzapper.model.entity.RateFeedbackFromUser;
import com.api.bugzapper.model.entity.RateFeedbackToCompany;
import com.api.bugzapper.model.request.RateFeedbackCompanyToUserRequest;
import com.api.bugzapper.model.request.RateFeedbackUserToCompanyRequest;
import com.api.bugzapper.repository.RateFeedbackRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.permissionService.PermissionService;
import com.api.bugzapper.service.userService.AppUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateFeedbackService{
    private final RateFeedbackRepository rateFeedbackRepository;
    private final AppUserService appUserService;
    private final CompanyService companyService;
    private final GetCurrentUser getCurrentUser;
    private final UserRoleRepository userRolesRepository;
    private final PermissionService permissionService;

    public RateFeedbackService(RateFeedbackRepository rateFeedbackRepository, AppUserService appUserService, CompanyService companyService, GetCurrentUser getCurrentUser, UserRoleRepository userRolesRepository, PermissionService permissionService) {
        this.rateFeedbackRepository = rateFeedbackRepository;
        this.appUserService = appUserService;
        this.companyService = companyService;
        this.getCurrentUser = getCurrentUser;
        this.userRolesRepository = userRolesRepository;
        this.permissionService = permissionService;
    }
    public RateFeedback userRateFeedbackToCompany(RateFeedbackUserToCompanyRequest request) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        appUserService.getUserById(currentUser.getUserId());
        companyService.getCompanyById(request.getCompanyId());
        if (request.getRateValue() > 5){
            throw new CustomNotFoundException("You are not allowed to rate more than 5 values.");
        }
//        Integer rate = rateFeedbackRepository.checkIfIsRateIsTrue(currentUser.getUserId(), request.getCompanyId());
//        if (rate > 0){
//            throw new CustomNotFoundException("This company has already rated to this user id : " + request.getCompanyId());
//        }

        return rateFeedbackRepository.userRateFeedbackToCompany(request, currentUser.getUserId());
    }
    public RateFeedback findById(Integer id) {
        RateFeedback response = rateFeedbackRepository.findById(id);
        if(response == null){
            throw new CustomNotFoundException("rate and feedback with id "+ id+ " not found.");
        }
        return response;
    }
    public RateFeedback companyRateFeedbackToUser(RateFeedbackCompanyToUserRequest request) {
//        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (!isCurrentUserAdminOfCompany(request.getCompanyId())) {
            throw new CustomNotFoundException("You are not an admin of the company.");
        }
        appUserService.getUserById(request.getUserId());

        companyService.getCompanyById(request.getCompanyId());
        if (request.getRateValue() > 5){
            throw new CustomNotFoundException("You are not allowed to rate more than 5 values.");
        }
//        Integer rate = rateFeedbackRepository.checkIfIsRate(request.getUserId(), request.getCompanyId());
//        if (rate > 0){
//            throw new CustomNotFoundException("This company has already rated to this user id : " + request.getUserId());
//        }

        return rateFeedbackRepository.companyRateFeedbackToUser(request, request.getUserId());
    }
    public RateFeedbackToCompany getRateAndFeedbackByUserId(Integer userId) {
        appUserService.getUserById(userId);
        return rateFeedbackRepository.getRateAndFeedbackByUserId(userId);
    }
    public List<RateFeedbackFromUser> getAllRateFeedbackOfCompany(Integer companyId) {
        companyService.getCompanyById(companyId);
        return rateFeedbackRepository.getAllRateFeedbackOfCompany(companyId);
    }
    public List<RateFeedbackFromCompanyToUser> getAllRateFeedbackOfCompanyToUser(Integer userId) {
        appUserService.getUserById(userId);
        return rateFeedbackRepository.getAllRateFeedbackOfCompanyToUser(userId);
    }

    private boolean isCurrentUserAdminOfCompany(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        return permissionService.isCompanyOwner(currentUser.getUserId(), companyId);
    }
}
