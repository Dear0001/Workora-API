package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.CompaniesDTO;
import com.api.bugzapper.model.dto.SubmissionDTO;
import com.api.bugzapper.model.dto.TasksDTO;
import com.api.bugzapper.model.entity.Task;
import com.api.bugzapper.model.request.AssignTaskRequest;
import com.api.bugzapper.model.request.TaskRequest;
import com.api.bugzapper.model.request.TaskSubmitRequest;
import com.api.bugzapper.model.request.UpdateTaskRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskRepository {
    @Select("""
                INSERT INTO task(task_name, description, status, due_date, created_at, attachment, phase_id, title, project_id)
                VALUES (#{task.taskName},#{task.taskDescription},#{task.taskStatus},#{task.dueDate},
                    CURRENT_TIMESTAMP, #{attachment}, #{task.phaseId}, #{task.title}, #{projectId})
                RETURNING *
            """)
    @Results(id = "taskMapper", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskDescription", column = "description"),
            @Result(property = "taskStatus", column = "status"),
            @Result(property = "dueDate", column = "due_date"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "deletedAt", column = "deleted_at"),
            @Result(property = "attachment", column = "attachment"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "phaseId", column = "phase_id",
                    one = @One(select = "com.api.bugzapper.repository.PhaseRepository.findById"))
    })
    Task createTask(@Param("task") TaskRequest taskRequest, String attachment, @Param("projectId") Integer projectId);

    @Select("""
                SELECT * FROM task WHERE task_id = #{id}
            """)
    @ResultMap("taskMapper")
    Task getTaskById(Integer id);

    @Select("""
                UPDATE task 
                SET task_name = #{task.taskName}, description = #{task.taskDescription}, due_date = #{task.dueDate}, 
                updated_at = CURRENT_TIMESTAMP, attachment = #{attachment}, phase_id = #{task.phaseId}, title = #{task.title},
                project_id = #{projectId}
                WHERE task_id = #{id} RETURNING *
            """)
    @ResultMap("taskMapper")
    Task updateTaskById(Integer id, @Param("task") UpdateTaskRequest taskRequest, String attachment, @Param("projectId") Integer projectId);

    @Delete("""
                DELETE FROM task WHERE task_id = #{id}
            """)
    @ResultMap("taskMapper")
    void deleteTaskById(Integer id);

    @Insert("""
                INSERT INTO task_submit(attachment, create_at, task_id, user_role_id) 
                Values(#{task.attachment}, current_timestamp, #{task.taskId}, #{userRoleId})
            """)
    void submitTask(@Param("task") TaskSubmitRequest taskSubmitRequest, Integer userRoleId);

    @Update("""
                UPDATE user_roles
                SET task_id = #{ass.taskId}, phase_id = #{ass.phaseId}, project_id = #{ass.projectId}
                WHERE user_id = #{userId} AND company_id = #{ass.companyId}
            """)
    void assignTaskToMemberInCompany(Integer userId, @Param("ass") AssignTaskRequest assignTaskRequest);

    @Select("""
                SELECT COUNT(*) FROM task t
                INNER JOIN phases p ON t.phase_id = p.phase_id
                INNER JOIN project pr ON p.project_id = pr.project_id
                WHERE t.task_id = #{taskId} AND pr.company_id = #{companyId}
            """)
    int isTaskInCompany(@Param("taskId") Integer taskId, @Param("companyId") Integer companyId);

    @Update("""
                UPDATE task SET status = #{status} WHERE task_id = #{taskId} AND phase_id = #{phaseId}
            """)
    @ResultMap("taskMapper")
    void updateTaskStatus(@Param("taskId") Integer taskId, @Param("phaseId") Integer phaseId, @Param("status") String status);

    @Select("""
            SELECT user_id
            FROM user_roles
            WHERE task_id = #{taskId}
            LIMIT 1
            """)
    Integer getUserIdByTaskId(@Param("taskId") Integer taskId);

    @Select("""
                SELECT * FROM task WHERE phase_id = #{phaseId}
            """)
    @ResultMap("taskMapper")
    List<Task> getAllTaskByPhaseId(Integer phaseId);

    @Select("""
                SELECT
                    ur.user_id,
                    u.first_name,
                    u.last_name,
                    u.avatar,
                    t.task_id,
                    t.task_name,
                    STRING_AGG(ts.attachment, ',') AS attachment
                FROM
                    task_submit ts
                        JOIN
                    task t ON ts.task_id = t.task_id
                        JOIN
                    user_roles ur ON ts.user_role_id = ur.user_role_id
                        JOIN
                    users u ON ur.user_id = u.user_id
                WHERE t.task_id = #{taskId}
                GROUP BY
                    ur.user_id,
                    u.first_name,
                    u.last_name,
                    u.avatar,
                    t.task_id,
                    t.task_name
                ORDER BY
                    ur.user_id,
                    t.task_id;
    """)
    @Results(id = "taskSubmission", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "attachment", column = "attachment"),
            @Result(property = "avatar", column = "avatar")
    })
    List<SubmissionDTO> getAllSubmitDataByTaskId(Integer taskId);

    @Select("""
        SELECT
            COUNT(DISTINCT ur.user_id)
        FROM
            task_submit ts
                JOIN
            task t ON ts.task_id = t.task_id
                JOIN
            user_roles ur ON ts.user_role_id = ur.user_role_id
                JOIN
            users u ON ur.user_id = u.user_id
        WHERE t.task_id = #{taskId}
        GROUP BY
            t.task_id;
    """)
    Integer countUserInATaskSubmission(Integer taskId);

    @Update("""
        UPDATE user_roles
        SET task_id = #{ass.taskId}
        WHERE user_id = #{userId} AND company_id = #{ass.companyId} AND phase_id = #{ass.phaseId}
    """)
    void updateTaskForMember(Integer userId, @Param("ass") AssignTaskRequest assignTaskRequest);

    @Insert("""
        INSERT INTO user_roles (user_id, role_id,company_id, project_id, phase_id, task_id) 
        VALUES (#{ass.userId}, #{roleId},#{ass.companyId}, #{ass.projectId}, #{ass.phaseId}, #{ass.taskId} )
    """)
    void insertTaskForMember(Integer userId,@Param("ass") AssignTaskRequest assignTaskRequest, Integer roleId);

    @Update("""
        UPDATE user_roles SET phase_id = #{ass.phaseId}, task_id = #{ass.taskId}
        WHERE user_id = #{userId} AND company_id = #{ass.companyId} AND project_id = #{ass.projectId}
    """)
    void updatePhaseForMember(Integer userId,@Param("ass") AssignTaskRequest assignTaskRequest);

    @Insert("""
        INSERT INTO user_roles (user_id, role_id,company_id, project_id, phase_id, task_id) 
        VALUES (#{ass.userId}, #{roleId},#{ass.companyId}, #{ass.projectId}, #{ass.phaseId}, #{ass.taskId} )
    """)
    void insertPhaseForMember(Integer userId, AssignTaskRequest assignTaskRequest, Integer roleId);

    @Select("""
        SELECT DISTINCT ON (ur.task_id) t.*
        FROM user_roles ur
                 JOIN task t ON ur.task_id = t.task_id
        WHERE ur.user_id = #{userId}
          AND ur.company_id = #{companyId}
          AND ur.project_id = #{projectId}
          AND ur.phase_id = #{phaseId}
        ORDER BY ur.task_id DESC;
    """)
    @Results(id = "taskDtoMapper", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskDescription", column = "description"),
            @Result(property = "taskStatus", column = "status"),
            @Result(property = "dueDate", column = "due_date"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "attachment", column = "attachment"),
            @Result(property = "projectId", column = "project_id"),
    })
    List<TasksDTO> getAllTaskByPhaseIdOfCurrentUser(Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
        SELECT t.task_id,
               t.task_name,
               t.status AS task_status,
               t.description AS task_description,
               t.created_at,
               t.due_date,
               t.attachment,
               t.project_id,
               t.phase_id,
               co.company_id,
               co.company_name
        FROM task t
        JOIN phases ph ON t.phase_id = ph.phase_id
        JOIN project pr ON ph.project_id = pr.project_id
        JOIN company co ON pr.company_id = co.company_id
        WHERE t.phase_id = #{phaseId}
        ORDER BY t.task_id
    """)
    @Results(id = "taskDtoMapperFull", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskStatus", column = "task_status"),
            @Result(property = "taskDescription", column = "task_description"),
            @Result(property = "dueDate", column = "due_date"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "attachment", column = "attachment"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
    })
    List<TasksDTO> getAllTasksDTOByPhaseId(Integer phaseId);

    @Select("""
            SELECT project_id FROM task WHERE task_id = #{taskId}
            """)
    Integer getProjectIdByTaskId(Integer taskId);

    @Select("""
            SELECT phase_id FROM task WHERE task_id = #{taskId}
            """)
    Integer getPhaseIdByTaskId(Integer taskId);

    @Select("""
            SELECT task_id FROM task WHERE project_id = #{projectId}
            """)
    List<Integer> getTaskIdsByProjectId(Integer projectId);

    @Select("""
            SELECT DISTINCT t.task_id FROM task t
            JOIN user_roles ur ON t.task_id = ur.task_id
            WHERE ur.user_id = #{userId} 
            AND ur.project_id = #{projectId}
            """)
    List<Integer> getTaskIdsByUserIdAndProjectId(Integer userId, Integer projectId);
}
