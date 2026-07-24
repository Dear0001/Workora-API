package com.api.bugzapper.repository;

import com.api.bugzapper.model.entity.Project;
import com.api.bugzapper.model.request.ProjectRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface ProjectRepository {
    @Select("""
            SELECT * FROM project WHERE project_id = #{id}
            """)
    @Results(id = "projectResult", value = {
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "deletedAt", column = "deleted_at"),
            @Result(property = "company", column = "company_id", one = @One(select = "com.api.bugzapper.repository.CompanyRepository.getCompanyById"))
    })
    Project getProjectById(Integer id);

    @Select("""
                INSERT INTO project (project_name,description,company_id)
                VALUES(#{project.projectName},#{project.description},#{project.companyId})
                RETURNING *
            """)
    @ResultMap("projectResult")
    Project createProject(@Param("project") ProjectRequest projectRequest);

    @Select("""
                UPDATE project
                SET project_name = #{projectRequest.projectName},
                    description = #{projectRequest.description}
                WHERE project_id = #{id} and company_id = #{projectRequest.companyId}
                RETURNING *
            """)
    @ResultMap("projectResult")
    Project updateProject(Integer id, @Param("projectRequest") ProjectRequest projectRequest);

    @Delete("""
                DELETE FROM project WHERE project_id = #{id}
            """)
    void deleteProject(Integer id);

    @Select("""
                SELECT * FROM project WHERE project_name = #{title}
            """)
    @ResultMap("projectResult")
    Project getProjectByTitle(String title);

    @Select("""
                SELECT * FROM project WHERE company_id = #{compantId}
            """)
    @ResultMap("projectResult")
    List<Project> getProjectByCompanyId(Integer companyId);

    @Select("""
            SELECT DISTINCT p.* FROM project p
            JOIN phases ph ON p.project_id = ph.project_id
            JOIN user_roles ur ON ph.phase_id = ur.phase_id
            WHERE ur.user_id = #{userId} AND p.company_id = #{companyId}
            """)
    @ResultMap("projectResult")
    List<Project> getProjectByCompanyIdAndPhaseMember(Integer userId, Integer companyId);

    @Select("""
                SELECT p.project_id FROM phases ph
                JOIN project p ON p.project_id = ph.project_id
                WHERE ph.phase_id = #{phaseId}
            """)
    Integer getProjectByPhaseId(Integer phaseId);

    @Select("""
        SELECT DISTINCT ON (ur.project_id) p.*
        FROM user_roles ur
                 JOIN project p ON ur.project_id = p.project_id
        WHERE ur.user_id = #{userId}
          AND ur.company_id = #{companyId}
        ORDER BY ur.project_id DESC;
    """)
    @ResultMap("projectResult")
    List<Project> getAllProjectByCompanyId(Integer userId, Integer companyId);

    @Select("""
            SELECT company_id FROM project WHERE project_id = #{projectId}
            """)
    Integer getCompanyIdByProjectId(Integer projectId);

    @Select("""
            SELECT project_id FROM project WHERE company_id = #{companyId}
            """)
    List<Integer> getProjectIdsByCompanyId(Integer companyId);

    @Select("""
            SELECT DISTINCT p.project_id FROM project p
            JOIN user_roles ur ON p.project_id = ur.project_id
            WHERE ur.user_id = #{userId} 
            AND p.company_id = #{companyId}
            AND ur.role_id = 2
            """)
    List<Integer> getProjectIdsByProjectManagerId(Integer userId, Integer companyId);

    @Select("""
            SELECT DISTINCT p.project_id FROM project p
            JOIN phases ph ON p.project_id = ph.project_id
            JOIN user_roles ur ON ph.phase_id = ur.phase_id
            WHERE ur.user_id = #{userId} AND p.company_id = #{companyId}
            """)
    List<Integer> getProjectIdsByPhaseMember(Integer userId, Integer companyId);

    @Select("""
            SELECT COUNT(*) FROM user_roles ur
            WHERE ur.user_id = #{userId}
              AND ur.project_id = #{projectId}
              AND ur.phase_id IS NOT NULL
            """)
    Integer countPhaseAssignmentsInProject(Integer userId, Integer projectId);
}
