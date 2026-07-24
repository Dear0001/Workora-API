package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.CompanyMemberDTO;
import com.api.bugzapper.model.dto.PhaseMemberDTO;
import com.api.bugzapper.model.dto.TaskMemberDTO;
import com.api.bugzapper.model.dto.TaskUserDTO;
import com.api.bugzapper.model.request.AssignTaskRequest;
import com.api.bugzapper.model.request.ProjectRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import com.api.bugzapper.model.entity.UserRole;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserRoleRepository {

    @Insert("""
                INSERT INTO user_roles (role_id, user_id, company_id)
                VALUES (#{switchId},#{userId}, #{companyId})
            """)
    void assignRoleToUserInCompany(Integer userId, Integer companyId, Integer switchId);

    @Update("""
                UPDATE user_roles
                SET role_id = #{roleId}
                WHERE user_role_id = #{userRoleId}
            """)
    // Scoped to a single row (not user_id + company_id) so a user with several
    // roles in one company (different projects/phases) doesn't get every row
    // overwritten by one switch-role call.
    int updateUserRole(@Param("userRoleId") Integer userRoleId, @Param("roleId") Integer roleId);

    @Select("""
               SELECT user_role_id FROM user_roles
               WHERE user_id = #{userId} AND company_id = #{companyId}
               ORDER BY
                   CASE WHEN project_id IS NULL AND phase_id IS NULL THEN 0 ELSE 1 END,
                   role_id ASC
               LIMIT 1
            """)
    // The user's "primary" (company-level, not project/phase-scoped) membership row.
    Integer findPrimaryUserRoleId(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
               SELECT user_role_id, role_id, user_id, company_id, task_id, phase_id
               FROM user_roles
               WHERE user_id = #{userId} AND company_id = #{companyId}
            """)
    @Results(id = "userRoleResultMap", value = {
            @Result(property = "userRoleId", column = "user_role_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "phasesId", column = "phase_id")
    })
    List<UserRole> findUserRole(@Param("userId") Integer userId, @Param("companyId") Integer companyId);


    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_role_id = #{id};
            """)
    Integer findUserRolesByUserRoleId(Integer id);

    @Select("""
            SELECT
                u.user_id AS userId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.email AS email,
                u.gender,
                u.avatar,
                (
                    SELECT r.role_name
                    FROM user_roles ur2
                    JOIN roles r ON ur2.role_id = r.role_id
                    WHERE ur2.user_id = u.user_id AND ur2.company_id = #{companyId}
                    ORDER BY r.role_id ASC
                    LIMIT 1
                ) AS roleName
            FROM users u
            JOIN user_roles ur ON u.user_id = ur.user_id
            JOIN roles r ON ur.role_id = r.role_id
            WHERE ur.company_id = #{companyId}
            GROUP BY u.user_id, u.first_name, u.last_name, u.email, u.gender, u.avatar
            ORDER BY MIN(r.role_id) ASC;
            """)
    List<CompanyMemberDTO> companyMembers(Integer companyId);

    @Select("""
            SELECT role_id FROM user_roles
            WHERE user_id = #{userId} AND company_id = #{companyId}
            ORDER BY
                CASE WHEN project_id IS NULL AND phase_id IS NULL THEN 0 ELSE 1 END,
                role_id ASC
            LIMIT 1
            """)
    Integer findUserRoleByUserIdAndCompanyId(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
            SELECT r.role_name
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId} AND ur.company_id = #{companyId}
            ORDER BY r.role_id ASC
            LIMIT 1
            """)
    String getRoleNameForUserInCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
            SELECT user_role_id FROM user_roles WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id IN (1,2) LIMIT 1
            """)
    Integer findUserRoleIdByUserIdAndCompanyId(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
                SELECT company_id 
                FROM user_roles
                WHERE user_role_id = #{userRoleId}
            """)
    Integer findCompanyIdByUserRoleId(Integer userRoleId);

    @Select("""
                SELECT user_id 
                FROM user_roles 
                WHERE user_role_id = #{userRoleId}
            """)
    Integer findUserIdByUserRoleId(Integer userRoleId);

    @Select("""
                SELECT DISTINCT user_id 
                FROM user_roles 
                WHERE project_id = #{projectId} AND role_id IN (1,2)
            """)
    List<Integer> findUserIdByProjectId(Integer projectId);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_id = #{userId} LIMIT 1
            """)
    Integer findUserRoleIdByUserId(Integer userId);

    @Select("""
                SELECT COUNT(*) FROM user_roles ur 
                WHERE user_id = #{userId} 
                AND company_id = #{companyId} 
                AND role_id IN (1, 2)
            """)
    Integer isAdminOrProjectManager(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
                SELECT COUNT(*) FROM user_roles
                WHERE user_id = #{userId} AND company_id = #{companyId}
            """)
    Integer isUserInCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
                SELECT COUNT(*) FROM user_roles
                WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId}
                AND phase_id = #{phaseId};
            """)
    Integer isUserInCompanyProjectPhase(@Param("userId") Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
                SELECT COUNT(*) FROM user_roles
                WHERE phase_id = #{phaseId} AND user_id = #{userId} 
                AND project_id = #{projectId} AND company_id = #{companyId}
            """)
    int isUserInPhase(Integer phaseId, Integer userId, Integer projectId, Integer companyId);


    @Insert("""
                INSERT INTO user_roles (user_id, company_id, project_id, role_id, phase_id)
                VALUES (#{userId}, #{companyId}, #{projectId}, #{roleId}, #{phaseId});
                
            """)
    void addMemberToPhase(Integer phaseId, Integer userId, Integer roleId, Integer companyId, Integer projectId);

    @Select("""
            SELECT DISTINCT u.user_id as userId, u.first_name as firstName,
                   u.last_name as lastName, u.email as email, u.gender, r.role_name as roleName, r.role_id as roleId, u.avatar as profileImage
            FROM users u
                     JOIN user_roles ur ON u.user_id = ur.user_id
                     JOIN roles r ON ur.role_id = r.role_id
            WHERE ur.phase_id = #{phaseId} AND r.role_id != 1
              """)
    List<PhaseMemberDTO> getPhaseMembers(@Param("phaseId") Integer phaseId);

    // Same as getPhaseMembers but also excludes PROJECT_MANAGER (role_id 2) — used for the
    // phase member list shown to users, since owner/PM shouldn't appear as phase members.
    // getPhaseMembers itself stays unfiltered-of-PM because it also backs isPhaseMember(),
    // an authorization check that must keep recognizing any legacy PM-in-phase row.
    @Select("""
            SELECT DISTINCT u.user_id as userId, u.first_name as firstName,
                   u.last_name as lastName, u.email as email, u.gender, r.role_name as roleName, r.role_id as roleId, u.avatar as profileImage
            FROM users u
                     JOIN user_roles ur ON u.user_id = ur.user_id
                     JOIN roles r ON ur.role_id = r.role_id
            WHERE ur.phase_id = #{phaseId} AND r.role_id NOT IN (1, 2)
              """)
    List<PhaseMemberDTO> getPhaseMembersForDisplay(@Param("phaseId") Integer phaseId);

    @Select("""
            SELECT DISTINCT u.user_id as userId,
                          u.first_name as firstName,
                          u.last_name as lastName,
                          u.email as email,
                          r.role_name as roleName,
                          u.avatar
          FROM users u
                   JOIN user_roles ur ON u.user_id = ur.user_id
                   JOIN roles r ON ur.role_id = r.role_id
          WHERE ur.task_id = #{taskId}
            AND r.role_id != 1
            AND r.role_id != 2;
            """)
    List<TaskMemberDTO> getTaskMember(@Param("taskId") Integer taskId);

    @Select("""
            SELECT u.user_id as userId, u.first_name as firstName, u.last_name as lastName, u.gender as gender, u.email as email, r.role_id as roleId, r.role_name as roleName, u.avatar as avatar
            FROM users u
            JOIN user_roles ur ON u.user_id = ur.user_id
            JOIN roles r ON ur.role_id = r.role_id
            WHERE ur.task_id = #{taskId} AND r.role_id != 1 AND r.role_id != 2
            """)
    List<TaskUserDTO> getTaskMembers(@Param("taskId") Integer taskId);

    @Insert("""
                Insert into user_roles (role_id, user_id) Values (#{roleId}, #{userId})
            """)
    void setUserRole(Integer userId, Integer roleId);

    @Update("""
               UPDATE user_roles SET phase_id = null, task_id = null
               WHERE phase_id = #{phaseId} AND user_id = #{userId}
            """)
    void removeMemberFromPhase(@Param("phaseId") Integer phaseId, @Param("userId") Integer userId);


    @Select("""
                SELECT COUNT(*) FROM user_roles
                WHERE user_id = #{userId}
                AND company_id = #{companyId}
            """)
    boolean isUserExistsInCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Delete("""
                DELETE FROM user_roles
                WHERE user_id = #{userId} AND company_id = #{companyId}
            """)
    void removeUserFromCompany(Integer userId, Integer companyId);

    @Select("""
                SELECT COUNT(*) FROM user_roles
                WHERE user_id = #{userId} AND phase_id = #{phaseId} AND company_id = #{companyId} AND project_id = #{projectId}
            """)
    boolean isUserExistsInPhase(Integer userId, Integer phaseId, Integer companyId, Integer projectId);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_id = #{userId}
            """)
    List<Integer> findUserRoleIdAsListByUserId(Integer userId);

    @Select("""
                SELECT DISTINCT user_id FROM user_roles WHERE company_id = #{companyId} AND role_id = 1
            """)
    List<Integer> findUserRoleIdByCompanyId(Integer companyId);

    @Select("""
                SELECT DISTINCT user_id FROM user_roles WHERE company_id = #{companyId} AND project_id = #{projectId} AND role_id IN (1,2)
            """)
    List<Integer> findUserRoleIdByCompanyIdAndProjectId(Integer companyId, Integer projectId);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE company_id = #{companyId} AND role_id = 1 LIMIT 1
            """)
    Integer findUserRoleIdByCompanyIdLimit1(Integer companyId);

    @Select("""
                SELECT ur.role_id
                FROM user_roles ur
                JOIN project p ON p.project_id = #{projectId}
                WHERE ur.user_id = #{userId}
                  AND ur.company_id = p.company_id
                  AND (ur.project_id = #{projectId} OR (ur.role_id = 1 AND ur.project_id IS NULL))
                ORDER BY
                    CASE ur.role_id
                        WHEN 1 THEN 0
                        WHEN 2 THEN 1
                        ELSE 2
                    END,
                    CASE WHEN ur.project_id = #{projectId} THEN 0 ELSE 1 END
                LIMIT 1
            """)
    Integer getRoleIdByUserIdAndProjectId(Integer userId, Integer projectId);

    @Insert("""
                INSERT INTO user_roles (user_id, role_id, company_id, project_id)
                VALUES (#{userId}, #{roleId}, #{companyId}, #{projectId});
            """)
    void insertProjectIdAndCompanyIdIntoUserRole(Integer userId, Integer roleId, Integer companyId, Integer projectId);

    @Insert("""
                INSERT INTO user_roles (user_id, role_id, company_id, project_id, phase_id)
                VALUES (#{userId}, #{roleId}, #{companyId}, #{projectId}, #{phaseId})
            """)
    void insertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(Integer userId, Integer roleId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_id = #{userId} AND role_id = 1 LIMIT 1
            """)
    Integer findUserRoleIdAdminByUserId(Integer currentUserId);

    @Select("""
                SELECT project_id FROM user_roles 
                    WHERE user_id = #{userId} AND company_id = #{companyId}
                    AND role_id = 1 LIMIT 1;
            """)
    Integer checkProjectIdInUserRoleByUserIdAndCompanyId(Integer userId, Integer companyId);

    @Update("""
                UPDATE user_roles SET project_id = #{projectId}
                WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id = 1;
            """)
    void updateInsertProjectIdAndCompanyIdIntoUserRole(Integer userId, Integer companyId, Integer projectId);

    @Select("""
                SELECT phase_id FROM user_roles 
                WHERE user_id = #{userId} AND company_id = #{companyId} 
                AND project_id = #{projectId}
                AND role_id IN (1,2) LIMIT 1;
            """)
    Integer getPhaseIdByUserIdAndCompanyIdAndProjectId(Integer userId, Integer companyId, Integer projectId);

    @Update("""
                UPDATE user_roles SET phase_id = #{phaseId}
                WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId} AND role_id = #{roleId};
            """)
    void updateInsertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(Integer userId, Integer roleId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
                SELECT task_id FROM user_roles
                WHERE user_id = #{userId} AND company_id = #{companyId} 
                AND project_id = #{projectId} AND phase_id = #{phaseId}
                AND role_id IN (1,2) LIMIT 1;
            """)
    Integer getTaskIdByUserIdAndCompanyIdAndProjectIdAndPhaseId(Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Update("""
                UPDATE user_roles SET task_id = #{taskId}
                WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId}
                AND phase_id = #{phaseId} AND role_id = #{roleId};
            """)
    void updateInsertTaskIdIntoUserRoleByUserIdAndCompanyIdAndProjectIdAndPhaseId(Integer userId, Integer companyId, Integer projectId, Integer phaseId, int taskId, Integer roleId);

    @Select("""
                SELECT role_id FROM user_roles WHERE user_id = #{userId} AND company_id = #{companyId}
                AND project_id = #{projectId} AND phase_id = #{phaseId} LIMIT 1;
            """)
    Integer getRoleIdByUserIdAndCompanyIdAndProjectIdAndPhaseId(Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Insert("""
                INSERT INTO user_roles (user_id, company_id, project_id, phase_id, task_id, role_id)
                VALUES (#{userId}, #{companyId}, #{projectId}, #{phaseId}, #{taskId}, #{roleId})
            """)
    void insertTaskIdIntoUserRoleByUserIdAndCompanyIdAndProjectIdAndPhaseId(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer taskId, Integer roleId);

    @Select("""
                SELECT user_roles.user_role_id FROM user_roles 
                WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id = 1 LIMIT 1
            """)
    Integer isAdminOfCompany(Integer userId, Integer companyId);

    @Select("""
                SELECT DISTINCT company_id FROM user_roles WHERE user_id = #{userId} 
                AND role_id = 1 AND company_id IS NOT NULL;    
            """)
    List<Integer> findCompanyIdAsAdminByUserId(Integer userId);

    @Select("""
                SELECT DISTINCT company_id FROM user_roles WHERE user_id = #{userId} 
                AND role_id IN (1,2) AND company_id IS NOT NULL AND project_id IS NOT NULL AND phase_id IS NOT NULL;    
            """)
    List<Integer> findCompanyIdAsAdminByUserIdWithNotNull(Integer userId);

    @Update("""
                UPDATE user_roles SET project_id = #{projectId}, role_id = 2 
                    WHERE user_id = #{userId} AND company_id = #{companyId} 
                        AND project_id IS NULL;
            """)
    void updateProjectManagerToUserRole(Integer userId, Integer companyId, Integer projectId);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_id = #{userId} AND company_id IS NULL;
            """)
    Integer checkCompanyIdIsNull(Integer userId);

    @Update("""
                UPDATE user_roles SET role_id = 1, company_id = #{companyId} WHERE user_role_id = #{userRoleId}
            """)
    // role_id 1 = COMPANY_OWNER. Scoped to the caller's placeholder (company_id IS NULL,
    // e.g. their BUG_HUNTER row from registration) row only, not every row for user_id.
    void updateCompanyIdToUserRole(Integer userRoleId, Integer companyId);

    @Select("""
                SELECT count(*) FROM user_roles WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId} AND phase_id IS NOT NULL;
            """)
    Integer isInProjectAndPhase(Integer userId, Integer companyId, Integer projectId);

    @Update("""
                UPDATE user_roles SET role_id = #{roleId}, phase_id = #{phaseId}
                              WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId}
            """)
    void updatePhaseIdForUser(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer roleId);

    @Insert("""
                INSERT INTO user_roles (user_id, company_id, project_id, phase_id, role_id) 
                VALUES (#{userId}, #{companyId}, #{projectId}, #{phaseId}, #{roleId});
            """)
    void insertPhaseIdForUserInUserRole(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer roleId);

    @Select("""
                SELECT COUNT(*) FROM user_roles 
                WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id = #{projectId}
                AND phase_id = #{phaseId} AND task_id = #{taskId}
            """)
    Integer isUserExistInTask(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer taskId);

    @Select("""
                SELECT COUNT(*) FROM user_roles ur 
                WHERE user_id = #{userId} 
                AND company_id = #{companyId}
                AND project_id = #{projectId}
                AND role_id IN (1, 2)
            """)
    Integer isAdminOrProjectManagerInProject(Integer userId, Integer companyId, Integer projectId);

    @Update("""
                UPDATE user_roles SET task_id = null WHERE task_id = #{taskId}
            """)
    void setTaskIdToNull(@Param("taskId") Integer id);

    @Update("""
                UPDATE user_roles SET phase_id = null, task_id = null WHERE phase_id = #{phaseId}
            """)
    void setPhaseIdToNull(@Param("phaseId") Integer id);

    @Update("""
                UPDATE user_roles SET project_id = null, phase_id = null, task_id = null WHERE project_id = #{projectId}
            """)
    void setProjectToNull(@Param("projectId") Integer id);

    @Select("""
                SELECT user_role_id FROM user_roles WHERE user_id = #{userId} AND phase_id = #{phaseId} LIMIT 1
            """)
    Integer findUserRoleIdByUserIdInPhaseId(Integer userId, Integer phaseId);

    @Select("""
                SELECT role_id FROM user_roles WHERE user_id = #{userId} AND phase_id = #{phaseId} LIMIT 1
            """)
    Integer getRoleIdByUserIdInPhaseId(@Param("userId") Integer userId, @Param("phaseId") Integer phaseId);

    @Update("""
                UPDATE user_roles SET role_id = #{roleId} WHERE user_id = #{userId} AND phase_id = #{phaseId}
            """)
    void updateUserRoleByPhaseId(Integer userId, Integer roleId, Integer phaseId);

    @Select("""
                SELECT user_id FROM user_roles WHERE project_id = #{id} AND role_id = 2 LIMIT 1;
            """)
    Integer getProjectManagerIdFromProject(Integer id);

    @Delete("""
             DELETE FROM user_roles                                                                                                                              
               WHERE project_id = #{id}  
                 AND role_id = 2;               

            """)
    void removeProjectManagerFromProject(@Param("id") Integer id);

    @Insert("""
                Insert Into user_roles(role_id, user_id, company_id, project_id) 
                VALUES (2,#{project.projectMangerId},#{project.companyId},#{id})
            """)
    void promoteProjectManagerToProject(@Param("project") ProjectRequest projectRequest, @Param("id") Integer id);

    @Select("""
                SELECT user_id FROM user_roles WHERE company_id = #{companyId} AND task_id = #{taskId} AND role_id IN (1,2)
            """)
    List<Integer> findUserIdByCompanyIdAndTaskId(Integer companyId, Integer taskId);

    @Update("""
                UPDATE user_roles SET role_id = 4 , project_id = null, phase_id = null, task_id = null WHERE project_id = #{id}

            """)
    // role_id 4 = DEVELOPER (fallback role when a member is detached from a removed project)
    void removeAllMemberFromProject(Integer id);

    @Select("""
                SELECT COUNT(*) FROM user_roles WHERE phase_id = phase_id AND user_id = #{userId}
            """)
    Integer isUserIsInPhase(Integer userId, Integer phaseId);

    @Select("""
                SELECT COUNT(*) FROM user_roles WHERE company_id = #{companyId} AND user_id = #{userId} AND project_id IS NULL
            """)
    Integer isInProjectInCompany(Integer userId, Integer companyId);

    @Select("""
                SELECT COUNT(*) FROM user_roles WHERE company_id = #{companyId} AND user_id = #{userId} AND project_id = #{projectId}
                AND phase_id = #{phaseId} AND task_id IS NULL;
            """)
    Integer isUserInTask(Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
                SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND company_id = #{ass.companyId}
                AND project_id = #{ass.projectId} AND phase_id != #{ass.phaseId}
            """)
    Integer isInAnotherPhase(Integer userId, @Param("ass") AssignTaskRequest assignTaskRequest);

    @Select("""
                SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND company_id = #{ass.companyId}
                AND project_id != #{ass.projectId}
            """)
    Integer isInAnotherProject(Integer userId,@Param("ass") AssignTaskRequest assignTaskRequest);

    @Insert("""
        INSERT INTO user_roles(role_id, user_id, company_id, project_id, phase_id, task_id)
        VALUES (#{roleId},#{userId}, #{ass.companyId}, #{ass.projectId}, #{ass.phaseId}, #{ass.taskId})
    """)
    void insertToProjectPhaseAndTask(Integer userId,@Param("ass") AssignTaskRequest assignTaskRequest, Integer roleId);

    @Insert("""
        INSERT INTO user_roles (role_id, user_id, company_id,project_id, phase_id) 
        VALUES (#{roleId}, #{userId}, #{companyId}, #{projectId}, #{phaseId});
    """)
    void insertToCompany(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer roleId);

    @Update("""
        UPDATE user_roles SET role_id = #{roleId}, company_id = #{companyId}, project_id = #{projectId}, phase_id = #{phaseId}
        WHERE user_id = #{userId} AND user_role_id = #{userRoleId}
    """)
    void updateToCompany(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer roleId, Integer userRoleId);

    @Update("""
        UPDATE user_roles SET role_id = #{roleId}, company_id = #{companyId}
        WHERE user_id = #{userId} AND user_role_id = #{userRoleId}
    """)
    void updateUserToCompany(Integer userId, Integer companyId, Integer roleId, Integer userRoleId);

    @Insert("""
        INSERT INTO user_roles (role_id, user_id, company_id, project_id)
        VALUES (2, #{userId}, #{companyId}, #{projectId})
    """)
    void insertProjectManagerToUserRole(Integer userId, Integer companyId, Integer projectId);

    @Update("""
        UPDATE user_roles SET role_id = #{roleId}, project_id = #{projectId}, phase_id = #{phaseId}
        WHERE user_id = #{userId} AND company_id = #{companyId} AND project_id IS NULL;
    """)
    void updateProjectIdForUser(Integer userId, Integer companyId, Integer projectId, Integer phaseId, Integer roleId);

    @Select("""
        SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id = 1
    """)
    Integer isAdminOfThePost(Integer userId, Integer companyId);

    @Select("""
        SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id IN (1, 5)
    """)
    // role_id 1 = COMPANY_OWNER, 5 = RECRUITER — both may manage recruitment posts/applications.
    Integer isOwnerOrRecruiter(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
        SELECT user_id FROM user_roles WHERE company_id = #{companyId} AND role_id = 1 LIMIT 1
    """)
    Integer findUserIdByCompanyId(Integer companyId);

    @Select("""
        SELECT COUNT(*) FROM user_roles ur1
        JOIN user_roles ur2 ON ur1.company_id = ur2.company_id
        WHERE ur1.user_id = #{userId1} AND ur2.user_id = #{userId2} AND ur1.company_id IS NOT NULL
    """)
    Integer sharesCompanyWith(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}
