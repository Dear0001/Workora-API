package com.api.bugzapper.service.postRecruitmentService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.PostRecruitmentNewsfeed;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.PostRecruitment;
import com.api.bugzapper.model.request.PostRecruitmentRequest;
import com.api.bugzapper.model.request.PostRecruitmentRequestForUpdate;
import com.api.bugzapper.repository.PostRecruitmentRepository;
import com.api.bugzapper.repository.ProjectRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostRecruitmentService {
    private final PostRecruitmentRepository postRecruitmentRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProjectRepository projectRepository;
    private final GetCurrentUser getCurrentUser;

    public PostRecruitmentService(
            PostRecruitmentRepository postRecruitmentRepository,
            UserRoleRepository userRoleRepository,
            ProjectRepository projectRepository,
            GetCurrentUser getCurrentUser) {
        this.postRecruitmentRepository = postRecruitmentRepository;
        this.userRoleRepository = userRoleRepository;
        this.projectRepository = projectRepository;
        this.getCurrentUser = getCurrentUser;
    }

    private Integer getCurrentUserId() {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("User not found");
        }
        return currentUser.getUserId();
    }

    /** Owner, RECRUITER, or a PROJECT_MANAGER in this company may manage its recruitment posts. */
    public boolean canManageRecruitmentForCompany(Integer userId, Integer companyId) {
        return userRoleRepository.isOwnerOrRecruiter(userId, companyId) > 0
                || userRoleRepository.isAdminOrProjectManager(userId, companyId) > 0;
    }
    public PostRecruitment createPostRecruitment(PostRecruitmentRequest postRecruitmentRequest) {
        Integer currentUserId = getCurrentUserId();

        if (!canManageRecruitmentForCompany(currentUserId, postRecruitmentRequest.getCompanyId())) {
            throw new CustomNotFoundException("Only the company owner, a project manager, or a recruiter can create recruitment posts");
        }

        String postRecruitmentJson;
        try {
            postRecruitmentJson = new ObjectMapper().writeValueAsString(postRecruitmentRequest.getPostData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Integer postRecruitmentId = postRecruitmentRepository.createPostRecruitment(postRecruitmentRequest, postRecruitmentJson);
        return getPostRecruitmentById(postRecruitmentId);
    }
    public PostRecruitment getPostRecruitmentById(Integer id) {
        PostRecruitment postRecruitment = postRecruitmentRepository.getPostRecruitmentById(id);
        if (postRecruitment == null) {
            throw new CustomNotFoundException("Post Recruitment with Id: " + id + " Not Found");
        }

        postRecruitment.setCanManage(canManageRecruitmentForCompany(getCurrentUserId(), postRecruitment.getCompanyId()));
        return postRecruitment;
    }
    public void deletePostRecruitmentById(Integer id) {
        PostRecruitment postRecruitment = getPostRecruitmentById(id);

        Integer currentUserId = getCurrentUserId();

        if (!canManageRecruitmentForCompany(currentUserId, postRecruitment.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to delete this post");
        }

        postRecruitmentRepository.deletePostRecruitmentById(id);
    }
    public List<PostRecruitmentNewsfeed> getAllPostRecruitment(Integer offset, Integer limit) {
        List<PostRecruitmentNewsfeed> postRecruitments = postRecruitmentRepository.getAllPostRecruitment(offset, limit);
        applyCanManage(postRecruitments);
        return postRecruitments;
    }
    public PostRecruitment updatePostRecruitmentById(PostRecruitmentRequestForUpdate postRecruitmentRequest, Integer id) {
        PostRecruitment postRecruitment = getPostRecruitmentById(id);

        Integer currentUserId = getCurrentUserId();

        if (!canManageRecruitmentForCompany(currentUserId, postRecruitment.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to update this post");
        }

        if (postRecruitmentRequest.getStatus() != null) {
            postRecruitmentRequest.setStatus(normalizeStatus(postRecruitmentRequest.getStatus()));
        }

        String postRecruitmentJson;
        try {
            postRecruitmentJson = new ObjectMapper().writeValueAsString(postRecruitmentRequest.getPostData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Integer postRecruitmentId = postRecruitmentRepository.updatePostRecruitmentById(postRecruitmentRequest, postRecruitmentJson, id);
        return getPostRecruitmentById(postRecruitmentId);
    }

    /** Owner/recruiter/PM-only: close or reopen a post without touching its other fields. */
    public PostRecruitment updatePostRecruitmentStatus(Integer id, String status) {
        PostRecruitment postRecruitment = getPostRecruitmentById(id);

        Integer currentUserId = getCurrentUserId();
        if (!canManageRecruitmentForCompany(currentUserId, postRecruitment.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to update this post");
        }

        postRecruitmentRepository.updateStatus(id, normalizeStatus(status));
        return getPostRecruitmentById(id);
    }

    private String normalizeStatus(String status) {
        if ("CLOSED".equalsIgnoreCase(status)) {
            return "CLOSED";
        }
        if ("OPEN".equalsIgnoreCase(status)) {
            return "OPEN";
        }
        throw new CustomNotFoundException("Status must be either OPEN or CLOSED");
    }

    /**
     * A PROJECT_MANAGER (who isn't also owner/recruiter) only sees posts scoped to a project they
     * manage, plus company-wide posts (no project). Owner/recruiter/anyone else sees everything.
     */
    public List<PostRecruitmentNewsfeed> getAllPostRecruitmentByCompanyId(Integer companyId, Integer offset, Integer limit) {
        List<PostRecruitmentNewsfeed> postRecruitments = postRecruitmentRepository.getAllPostRecruitmentByCompanyId(companyId, offset, limit);
        applyCanManage(postRecruitments);

        Integer currentUserId = getCurrentUserId();
        boolean isOwnerOrRecruiter = userRoleRepository.isOwnerOrRecruiter(currentUserId, companyId) > 0;
        boolean isProjectManagerOnly = !isOwnerOrRecruiter && userRoleRepository.isProjectManagerOfCompany(currentUserId, companyId) > 0;

        if (isProjectManagerOnly) {
            List<Integer> managedProjectIds = projectRepository.getProjectIdsByProjectManagerId(currentUserId, companyId);
            postRecruitments = postRecruitments.stream()
                    .filter(post -> post.getProjectId() == null || managedProjectIds.contains(post.getProjectId()))
                    .collect(Collectors.toList());
        }

        return postRecruitments;
    }

    private void applyCanManage(List<PostRecruitmentNewsfeed> postRecruitments) {
        Integer currentUserId = getCurrentUserId();
        for (PostRecruitmentNewsfeed post : postRecruitments) {
            post.setCanManage(canManageRecruitmentForCompany(currentUserId, post.getCompanyId()));
        }
    }

}
