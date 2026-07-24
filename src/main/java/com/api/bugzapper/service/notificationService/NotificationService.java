package com.api.bugzapper.service.notificationService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.NotificationDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Notification;
import com.api.bugzapper.repository.NotificationRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final GetCurrentUser getCurrentUser;
    private final UserRoleRepository userRoleRepository;

    public NotificationService(NotificationRepository notificationRepository, GetCurrentUser getCurrentUser, UserRoleRepository userRoleRepository) {
        this.notificationRepository = notificationRepository;
        this.getCurrentUser = getCurrentUser;
        this.userRoleRepository = userRoleRepository;
    }
    public void createNotification(Notification notification) {
        notificationRepository.createNotification(notification);
    }
    public List<NotificationDTO> getAllNotificationsByUserRoleId(Integer userRoleId) {
        List<NotificationDTO> notificationDTO = notificationRepository.getAllNotificationsByUserRoleId(userRoleId);

        if (notificationDTO == null) {
            return null;
        }
        return notificationDTO;
    }
    public NotificationDTO getNotificationById(Integer id) {
        NotificationDTO notificationDTO = notificationRepository.getNotificationById(id);
        if (notificationDTO == null) {
            throw new CustomNotFoundException("Notification with id " + id + " not found");
        }
        return notificationDTO;
    }
    public void updateNotificationStatus(Integer id, Boolean isRead) {
        getNotificationById(id);
        notificationRepository.updateNotificationStatus(id, isRead);
    }
    public void deleteNotification(Integer id) {
        NotificationDTO notification = getNotificationById(id);

        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer recipientUserId = notification.getUserRoleId() != null
                ? userRoleRepository.findUserIdByUserRoleId(notification.getUserRoleId())
                : null;
        if (currentUser == null || recipientUserId == null || !recipientUserId.equals(currentUser.getUserId())) {
            throw new CustomNotFoundException("You do not have permission to delete this notification.");
        }

        notificationRepository.delete(id);
    }
    public void updateNotificationToRead(Integer id) {
        getNotificationById(id);
        notificationRepository.updateNotificationToRead(id);
    }
    public void clearPhaseReference(Integer phaseId) {
        notificationRepository.setPhaseIdToNull(phaseId);
    }
    public Integer countUnreadNotification() {
        AppUser currenUser = getCurrentUser.getCurrentUser();

        List<Integer> userRoleIds = userRoleRepository.findUserRoleIdAsListByUserId(currenUser.getUserId());
        if (userRoleIds == null || userRoleIds.isEmpty()) {
            return 0;
        }
        return notificationRepository.countUnreadNotification(userRoleIds);
    }
}
