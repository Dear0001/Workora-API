package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.NotificationDTO;
import com.api.bugzapper.model.entity.Notification;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Mapper
public interface NotificationRepository {
    @Insert("""
        INSERT INTO notification(title, description, status, company_id, project_id, phase_id, task_id, report_id, apply_id, user_id, user_role_id, created_at, is_read) 
        VALUES (#{not.title}, #{not.description}, #{not.status}, #{not.companyId}, #{not.projectId}, #{not.phaseId}, #{not.taskId}, #{not.reportId} , #{not.applyId}, #{not.userId}, #{not.userRoleId}, current_timestamp, false)
    """)
    void createNotification(@Param("not") Notification notification);

    @Select("""
      SELECT n.* FROM notification n
      WHERE 
        user_role_id = #{userRoleId}
      ORDER BY 
        created_at DESC
    """)
    @Results(id = "notMapper", value = {
            @Result(property = "notificationId", column = "notification_id"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "reportId", column = "report_id"),
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "avatar", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getAvatarByUserId")),
            @Result(property = "userRoleId", column = "user_role_id")
    })
    List<NotificationDTO> getAllNotificationsByUserRoleId(@Param("userRoleId") Integer userRoleId);

    @Select("""
        SELECT * FROM notification WHERE notification_id = #{id}
    """)
    @ResultMap("notMapper")
    NotificationDTO getNotificationById(Integer id);

    @Update("""
        UPDATE notification SET is_read = #{isRead} WHERE notification_id = #{id}
    """)
    void updateNotificationStatus(Integer id, Boolean isRead);

    @Delete("""
        DELETE FROM notification WHERE notification_id = #{id}
    """)
    void delete(Integer id);

    @Update("""
        UPDATE notification SET phase_id = NULL WHERE phase_id = #{phaseId}
    """)
    void setPhaseIdToNull(Integer phaseId);

    @Update("""
        UPDATE notification SET is_read = true WHERE notification_id = #{id}
    """)
    void updateNotificationToRead(Integer id);

    @Select("""
        <script>
        SELECT COUNT(*) FROM notification n
        WHERE is_read = false
        AND user_role_id IN
        <foreach collection="userRoleIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    Integer countUnreadNotification(@Param("userRoleIds") List<Integer> userRoleIds);
}
