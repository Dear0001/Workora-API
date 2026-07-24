package com.api.bugzapper.service.taskService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.model.dto.SubmissionDTO;
import com.api.bugzapper.model.dto.TaskUserDTO;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.request.*;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.TaskMemberDTO;
import com.api.bugzapper.repository.*;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.phaseService.PhaseService;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final PhaseService phaseService;
    private final AppUserService appUserService;
    private final NotificationService notificationService;
    private final UserRoleRepository userRoleRepository;
    private final EmailUtil emailUtil;
    private final GetCurrentUser getCurrentUser;
    private final PhaseRepository phaseRepository;
    private final ProjectService projectService;
    private final CompanyService companyService;
    public Task createTask(TaskRequest taskRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        phaseService.findById(taskRequest.getPhaseId());
        Integer projectId = projectService.getProjectByPhaseId(taskRequest.getPhaseId());
        Company company = companyService.getCompanyByProjectId(projectId);

        // Check if user is admin or project manager at company level
        Integer isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());

        // Also check at phase level as secondary check
        boolean isAdmin = companyService.isTrue(currentUser.getUserId(), company.getCompanyId(), projectId, taskRequest.getPhaseId()) > 0;

        if (isAdminOrProjectManager < 1 && !isAdmin) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }
        phaseService.findById(taskRequest.getPhaseId());

        String attachment = String.join(",", taskRequest.getAttachment());

        Task task = taskRepository.createTask(taskRequest, attachment, projectId);

        Integer taskId = userRoleRepository.getTaskIdByUserIdAndCompanyIdAndProjectIdAndPhaseId(currentUser.getUserId(),company.getCompanyId(), projectId,taskRequest.getPhaseId());
        Integer roleId = userRoleRepository.getRoleIdByUserIdAndCompanyIdAndProjectIdAndPhaseId(currentUser.getUserId(), company.getCompanyId(), projectId, taskRequest.getPhaseId());

        // If roleId is null, use company-level role (admin or project manager already validated)
        if (roleId == null) {
            roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(currentUser.getUserId(), company.getCompanyId());
        }

        List<Integer> userIds = taskRequest.getUserIds();

        // Assign the task to each user in the list
        for (Integer userId : userIds) {
            // Check if the user is a member of the specified company
            Integer isUserInCompany = userRoleRepository.isUserInCompany(userId, company.getCompanyId());
            if (isUserInCompany == 0) {
                throw new CustomNotFoundException("User with ID " + userId + " is not a member of the company");
            }

            Integer isUserExistInTask = userRoleRepository.isUserExistInTask(userId, company.getCompanyId(),projectId, taskRequest.getPhaseId(), task.getTaskId());

            if (isUserExistInTask > 0) {
                throw new CustomNotFoundException("User with ID " + userId + " is already in task id : " + task.getTaskId());
            }

            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdInPhaseId(userId, taskRequest.getPhaseId());
            AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);

            String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has assigned a task";

            Notification notification = Notification.builder()
                    .title("Assign task notification")
                    .description(applicantName)
                    .status("task")
                    .companyId(company.getCompanyId())
                    .projectId(projectId)
                    .phaseId(taskRequest.getPhaseId())
                    .taskId(task.getTaskId())
                    .userId(currentUser.getUserId()) // This id is the assign task owner
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "assign_task");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }

            // Insert notification to its table
            notificationService.createNotification(notification);

            AssignTaskRequest assignTaskRequest = new AssignTaskRequest(
                    null,
                    company.getCompanyId(),
                    projectId,
                    taskRequest.getPhaseId(),
                    task.getTaskId()
            );
            Integer roleIdForUser = userRoleRepository.findUserRoleByUserIdAndCompanyId(userId, company.getCompanyId());
            Integer isInProject = userRoleRepository.isInProjectInCompany(userId, company.getCompanyId());
            if (isInProject > 0){
                // Assign the task to the user
                taskRepository.assignTaskToMemberInCompany(userId, assignTaskRequest);
            }else{
                Integer isInAnotherProject = userRoleRepository.isInAnotherProject(userId, assignTaskRequest);
                if (isInAnotherProject > 0){
                    Integer isInPhase = userRoleRepository.isUserIsInPhase(userId, taskRequest.getPhaseId());
                    if (isInPhase > 0){
                        Integer isInTask = userRoleRepository.isUserInTask(userId, company.getCompanyId(), projectId, taskRequest.getPhaseId());
                        if (isInTask > 0){
                            // Assign the task to the user
                            taskRepository.updateTaskForMember(userId, assignTaskRequest);
                        }else{
                            taskRepository.insertTaskForMember(userId, assignTaskRequest, roleIdForUser);
                        }
                    }else{
                        Integer isInAnotherPhase = userRoleRepository.isInAnotherPhase(userId, assignTaskRequest);
                        if (isInAnotherPhase > 0){
                            taskRepository.insertPhaseForMember(userId, assignTaskRequest, roleIdForUser);
                        }else {
                            taskRepository.updatePhaseForMember(userId, assignTaskRequest);
                        }
                    }
                }else {
                    userRoleRepository.insertToProjectPhaseAndTask(userId, assignTaskRequest, roleIdForUser);
                }
            }

        }

        task.setUsers(getAllTaskMembers(task.getTaskId()));
        if (taskId == null) {
            userRoleRepository.updateInsertTaskIdIntoUserRoleByUserIdAndCompanyIdAndProjectIdAndPhaseId(currentUser.getUserId(), company.getCompanyId(), projectId, taskRequest.getPhaseId(), task.getTaskId(), roleId);
            return task;
        }

        userRoleRepository.insertTaskIdIntoUserRoleByUserIdAndCompanyIdAndProjectIdAndPhaseId(currentUser.getUserId(), company.getCompanyId(), projectId, taskRequest.getPhaseId(), task.getTaskId(), roleId);
        return task;
    }
    public Task getTaskById(Integer id) {
        Task task = taskRepository.getTaskById(id);
        if (task == null) {
            throw new CustomNotFoundException("No task : " + id + " found");
        }

        // Permission check: Only allow task view if user is:
        // 1. Task owner/assignee (developer)
        // 2. Project manager
        // 3. Admin
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found.");
        }

        Integer projectId = projectService.getProjectByPhaseId(task.getPhaseId().getId());
        Company company = companyService.getCompanyByProjectId(projectId);

        // Check if user is admin or project manager
        boolean isAdminOrPM = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId()) > 0;
        boolean isCompanyOwner = companyService.isOwnerOfCompany(currentUser.getUserId(), company.getCompanyId()) > 0;

        if (!isAdminOrPM && !isCompanyOwner) {
            // Check if user is assigned to this task
            Integer isAssigned = userRoleRepository.isUserExistInTask(
                currentUser.getUserId(),
                company.getCompanyId(),
                projectId,
                task.getPhaseId().getId(),
                id
            );
            if (isAssigned < 1) {
                throw new CustomNotFoundException("You are not authorized to view this task.");
            }
        }

        task.setUsers(getAllTaskMembers(id));
        return task;
    }
    public Task updateTaskById(Integer id, UpdateTaskRequest taskRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        getTaskById(id);
        Integer phaseId = phaseService.getPhaseByTaskId(id);
        Integer projectId = projectService.getProjectByPhaseId(taskRequest.getPhaseId());
        Company company = companyService.getCompanyByProjectId(projectId);

        // Check if user is admin or project manager at company level
        Integer isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());

        // Also check at phase level as secondary check
        boolean isAdmin = companyService.isTrue(currentUser.getUserId(),company.getCompanyId(),projectId, taskRequest.getPhaseId()) > 0;

        if (isAdminOrProjectManager < 1 && !isAdmin) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }
        // Assign the task to each user in the list
        for (Integer userId : taskRequest.getUserIds()) {
            // Check if the user is a member of the specified company
            Integer isUserInCompany = userRoleRepository.isUserInCompany(userId, company.getCompanyId());
            if (isUserInCompany == 0) {
                throw new CustomNotFoundException("User with ID " + userId + " is not a member of the company");
            }

            Integer isUserExistInTask = userRoleRepository.isUserExistInTask(userId, company.getCompanyId(),projectId, taskRequest.getPhaseId(), id);

            if (isUserExistInTask > 0) {
                throw new CustomNotFoundException("User with ID " + userId + " is already in task id : " + id);
            }

            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdInPhaseId(userId, taskRequest.getPhaseId());
            AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);

            String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has assigned a task";

            Notification notification = Notification.builder()
                    .title("Assign task notification")
                    .description(applicantName)
                    .status("task")
                    .companyId(company.getCompanyId())
                    .projectId(projectId)
                    .phaseId(phaseId)
                    .taskId(id)
                    .userId(currentUser.getUserId()) // This id is the assign task owner
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "assign_task");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }

            // Insert notification to its table
            notificationService.createNotification(notification);

            AssignTaskRequest assignTaskRequest = new AssignTaskRequest(
                    null,
                    company.getCompanyId(),
                    projectId,
                    taskRequest.getPhaseId(),
                    id
            );
            // Assign the task to the user
            taskRepository.assignTaskToMemberInCompany(userId, assignTaskRequest);
        }

        String attachment = String.join(",", taskRequest.getAttachment());
        Task task = taskRepository.updateTaskById(id, taskRequest, attachment, projectId);
        task.setUsers(getAllTaskMembers(task.getTaskId()));
        return task;
    }
    public void deleteTaskById(Integer id) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Task task = getTaskById(id);
        Integer projectId = projectService.getProjectByPhaseId(task.getPhaseId().getId());
        Company company = companyService.getCompanyByProjectId(projectId);

        // Check if user is admin or project manager at company level
        Integer isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());

        // Also check at phase level as secondary check
        boolean isAdmin = companyService.isTrue(currentUser.getUserId(), company.getCompanyId(), projectId, task.getPhaseId().getId()) > 0;

        if (isAdminOrProjectManager < 1 && !isAdmin) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }

        userRoleRepository.setTaskIdToNull(id);
        taskRepository.deleteTaskById(id);
    }
    public void submitTask(TaskSubmitRequest taskSubmitRequest){
        AppUser currentUser = getCurrentUser.getCurrentUser();

        Task task = getTaskById(taskSubmitRequest.getTaskId());
        Integer phaseId = phaseService.getPhaseByTaskId(task.getTaskId());
        Integer projectId = projectService.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);

        // Check if the current user is assigned to this task
        Integer isAssigned = userRoleRepository.isUserExistInTask(
            currentUser.getUserId(),
            company.getCompanyId(),
            projectId,
            phaseId,
            taskSubmitRequest.getTaskId()
        );

        if (isAssigned < 1) {
            throw new CustomNotFoundException("You are not assigned to this task. You cannot submit it.");
        }

        AppUserDTO appUserDTO = appUserService.getUserDtoById(currentUser.getUserId());
        List<Integer> userIds = userRoleRepository.findUserIdByCompanyIdAndTaskId(company.getCompanyId(), task.getTaskId());
        Integer userSubmitterRoleId = userRoleRepository.findUserRoleIdByUserIdInPhaseId(currentUser.getUserId(), phaseId);

        String applicantName = appUserDTO.getFirstName() + " " + appUserDTO.getLastName() + " has submit a task";

        for (Integer userId : userIds ){
            AppUserDTO userReceiver = appUserService.getUserDtoById(userId);
            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdInPhaseId(userId, phaseId);
            Notification notification = Notification.builder()
                    .title("Task submit notification")
                    .description(applicantName)
                    .status("task")
                    .companyId(company.getCompanyId())
                    .projectId(projectId)
                    .phaseId(phaseId)
                    .taskId(task.getTaskId())
                    .userId(currentUser.getUserId()) // This id is the submit owner
                    .userRoleId(userRoleId) // This is the task owner
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), currentUser.getUserId(), notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(userReceiver.getEmail(), applicantName, "submit_task");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }

            // Insert notification to its table
            notificationService.createNotification(notification);
        }

        taskRepository.submitTask(taskSubmitRequest, userSubmitterRoleId);
    }
    public void assignTaskToMemberInCompany(AssignTaskRequest assignTaskRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Company company = companyService.getCompanyById(assignTaskRequest.getCompanyId());
        Project project = projectService.getProjectById(assignTaskRequest.getProjectId());
        Phase phase = phaseService.findById(assignTaskRequest.getPhaseId());
        getTaskById(assignTaskRequest.getTaskId());

        Integer projectId = projectService.getProjectByPhaseId(assignTaskRequest.getPhaseId());

        if (projectId == null) {
            throw new CustomNotFoundException("This phase doesn't exist in any project");
        }

        if (!projectId.equals(assignTaskRequest.getProjectId())) {
            throw new CustomNotFoundException("This phase doesn't exist in " + assignTaskRequest.getProjectId() + " project");
        }
        // Check if the current user is an admin or a project manager in the specified company
        boolean isAdminOrPM = companyService.isTrue(currentUser.getUserId(), assignTaskRequest.getCompanyId(), projectId, assignTaskRequest.getPhaseId()) > 0;
        if (!isAdminOrPM) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }

        // Check if the task belongs to the specified company
        int taskInCompany = taskRepository.isTaskInCompany(assignTaskRequest.getTaskId(), assignTaskRequest.getCompanyId());
        if (taskInCompany == 0) {
            throw new CustomNotFoundException("The task does not belong to the specified company.");
        }

        // Check if the phase belongs to the specified company
        int phaseInCompany = phaseRepository.isPhaseInCompany(assignTaskRequest.getPhaseId(), assignTaskRequest.getCompanyId());
        if (phaseInCompany == 0) {
            throw new CustomNotFoundException("The phase does not belong to the specified company.");
        }

        List<Integer> userIds = assignTaskRequest.getUserIds();

        // Assign the task to each user in the list
        for (Integer userId : userIds) {
            // Check if the user is a member of the specified company
            Integer isUserInCompany = userRoleRepository.isUserInCompany(userId, assignTaskRequest.getCompanyId());
            if (isUserInCompany < 1) {
                throw new CustomNotFoundException("User with ID " + userId + " is not a member of the company");
            }

            Integer isUserExistInTask = userRoleRepository.isUserExistInTask(userId, assignTaskRequest.getCompanyId(),assignTaskRequest.getProjectId(), assignTaskRequest.getPhaseId(), assignTaskRequest.getTaskId());

            if (isUserExistInTask > 0) {
                throw new CustomNotFoundException("User with ID " + userId + " is already in task id : " + assignTaskRequest.getTaskId());
            }

            Integer userRoleId = userRoleRepository.findUserRoleIdByUserId(userId);
            AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);

            String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has assigned a task";

            Notification notification = Notification.builder()
                    .title("Assign task notification")
                    .description(applicantName)
                    .status("task")
                    .companyId(company.getCompanyId())
                    .projectId(project.getProjectId())
                    .phaseId(phase.getId())
                    .taskId(assignTaskRequest.getTaskId())
                    .userId(currentUser.getUserId()) // This id is the assign task owner
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "assign_task");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }

            // Insert notification to its table
            notificationService.createNotification(notification);

            // Assign the task to the user
            taskRepository.assignTaskToMemberInCompany(userId, assignTaskRequest);

        }
    }
    public List<TaskMemberDTO> getAllTaskMember(Integer taskId) {
        // Permission check: verify user can access this task
        getTaskById(taskId);
        return userRoleRepository.getTaskMember(taskId);
    }
    public List<TaskUserDTO> getAllTaskMembers(Integer taskId) {
        return userRoleRepository.getTaskMembers(taskId);
    }
    public void updateTaskStatus(Integer taskId, Integer phaseId, Integer companyId, TaskForStatus taskForStatus) {
        AppUser currentUser = getCurrentUser.getCurrentUser();

        // Check if the task exists
        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            throw new CustomNotFoundException("No task with ID: " + taskId + " found");
        }
        Integer projectId = projectService.getProjectByPhaseId(phaseId);
        
        // Retrieve the phase ID associated with the task
        Integer taskPhaseId = task.getPhaseId().getId();

        // Check if the phase ID provided matches the phase ID associated with the task
        if (!taskPhaseId.equals(phaseId)) {
            throw new CustomNotFoundException("Phase ID does not match with the task: " + phaseId);
        }

        // Check company exists
        Company company = companyService.getCompanyById(companyId);
        if (company == null) {
            throw new CustomNotFoundException("Company with ID: " + companyId + " not found");
        }

        // Check if the status is valid
        if (!taskForStatus.getTaskStatus().equals("completed") && !taskForStatus.getTaskStatus().equals("onProgress") && !taskForStatus.getTaskStatus().equals("notYet")) {
            throw new CustomNotFoundException("Invalid status: " + taskForStatus.getTaskStatus());
        }

        // Permission check: Allow update if:
        // 1. User is admin or project manager
        // 2. User is assigned to this task (developer can update their own task status)
        Integer isAdminOrPM = userRoleRepository.isAdminOrProjectManagerInProject(currentUser.getUserId(), companyId, projectId);
        Integer isAssigned = userRoleRepository.isUserExistInTask(currentUser.getUserId(), companyId, projectId, phaseId, taskId);

        if (isAdminOrPM < 1 && isAssigned < 1) {
            throw new CustomNotFoundException("You do not have permission to update this task status.");
        }

        List<Integer> userIds = userRoleRepository.findUserRoleIdByCompanyId(companyId);

        String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has updated a task progress";

        for (Integer idUser : userIds) {
            Integer userRoleId = userRoleRepository.findUserRoleIdByUserId(idUser);
            AppUserDTO appUser = appUserService.getUserDtoById(idUser);
            Notification notification = Notification.builder()
                    .title("Update task status notification")
                    .description(applicantName)
                    .status("task")
                    .companyId(company.getCompanyId())
                    .projectId(projectId)
                    .phaseId(phaseId)
                    .taskId(taskId)
                    .userId(currentUser.getUserId()) // This id is the user that updated task status
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), idUser, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUser.getEmail(), applicantName, "updated_task");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }
            // Insert notification to its table
            notificationService.createNotification(notification);
        }

        // Update the status
        taskRepository.updateTaskStatus(taskId, phaseId, taskForStatus.getTaskStatus());
    }
    public List<Task> getAllTaskByPhaseId(Integer phaseId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();

        if (currentUser == null) {
            throw new CustomNotFoundException("User not found.");
        }
        phaseService.findById(phaseId);

        List<Task> allTasks = taskRepository.getAllTaskByPhaseId(phaseId);

        Integer projectId = projectService.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);

        Integer isAdminOrPM = userRoleRepository.isAdminOrProjectManagerInProject(
                currentUser.getUserId(), company.getCompanyId(), projectId);
        boolean isCompanyOwner = companyService.isOwnerOfCompany(
                currentUser.getUserId(), company.getCompanyId()) > 0;

        if (isCompanyOwner || (isAdminOrPM != null && isAdminOrPM > 0)) {
            allTasks.forEach(task -> task.setUsers(getAllTaskMembers(task.getTaskId())));
            return allTasks;
        }

        // Non-managers only see tasks they are assigned to
        List<Task> filteredTasks = new java.util.ArrayList<>();
        for (Task task : allTasks) {
            Integer isAssigned = userRoleRepository.isUserExistInTask(
                currentUser.getUserId(),
                company.getCompanyId(),
                projectId,
                phaseId,
                task.getTaskId()
            );
            if (isAssigned > 0) {
                task.setUsers(getAllTaskMembers(task.getTaskId()));
                filteredTasks.add(task);
            }
        }

        return filteredTasks;
    }
    public List<SubmissionDTO> getAllSubmitDataByTaskId(Integer taskId) {
        getTaskById(taskId);

        return taskRepository.getAllSubmitDataByTaskId(taskId);
    }
    public Integer countUserInATaskSubmission(Integer taskId) {
        getTaskById(taskId);
        return taskRepository.countUserInATaskSubmission(taskId);
    }

    public Integer getProjectIdForTask(Integer taskId) {
        Task task = taskRepository.getTaskById(taskId);
        if (task == null) {
            throw new CustomNotFoundException("No task : " + taskId + " found");
        }
        return projectService.getProjectByPhaseId(task.getPhaseId().getId());
    }
}
