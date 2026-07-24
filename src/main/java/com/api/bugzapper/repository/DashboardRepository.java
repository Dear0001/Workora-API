package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.*;
import com.api.bugzapper.model.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DashboardRepository {
    @Select("""
        WITH Counts AS (
            SELECT
                -- Count of distinct companies the user is involved in
                COUNT(DISTINCT ur.company_id) AS company_count,
                -- Count of distinct projects the user is involved in
                COUNT(DISTINCT ur.project_id) AS project_count,
                -- Count of distinct tasks the user is involved in
                COUNT(DISTINCT ur.task_id) AS task_count
            FROM
                users u
                    JOIN user_roles ur ON u.user_id = ur.user_id
            WHERE
                u.user_id = #{userId}
        ),
             ReportCounts AS (
                 SELECT
                     COUNT(DISTINCT report_id) AS report_count
                 FROM
                     report r
                         JOIN phases ph ON r.phase_id = ph.phase_id
                         JOIN project p ON ph.project_id = p.project_id
                         JOIN company c ON p.company_id = c.company_id
                         JOIN user_roles ur ON c.company_id = ur.company_id
                         JOIN users u ON ur.user_id = u.user_id
                 WHERE
                       ur.role_id IN (1, 2)
                   AND ur.user_id = #{userId}
             )
        SELECT
            company_count,
            project_count,
            task_count,
            report_count
        FROM
            Counts
                CROSS JOIN
            ReportCounts;
    """)
    @Results(id = "dashboardMapper", value = {
            @Result(property = "companyCount", column = "company_count"),
            @Result(property = "projectCount", column = "project_count"),
            @Result(property = "taskCount", column = "task_count"),
            @Result(property = "reportCount", column = "report_count")
    })
    Dashboard getAllCountByUserId(Integer userId);

    @Select("""
        SELECT
            DISTINCT c.company_id,
            c.company_name,
            c.profile_image,
            c.cover_image,
            c.description,
            c.created_at,
            c.email,
            c.address,
            c.phone,
            r.role_name
        FROM company c
                 JOIN user_roles ur ON c.company_id = ur.company_id
                 JOIN roles r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId} AND ur.role_id !=1
        ORDER BY c.company_id DESC
        LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @Results(id = "companyMapper", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "email", column = "email"),
            @Result(property = "address", column = "address"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "roleName", column = "role_name")
    })
    List<CompaniesDTO> getAllCompanyByUserId(Integer offset, Integer limit, Integer userId);

    @Select("""
        SELECT
            DISTINCT ON (c.company_id)
            c.company_id,
            c.company_name,
            c.profile_image,
            c.cover_image,
            c.description,
            c.created_at,
            c.email,
            c.address,
            c.phone,
            r.role_name
        FROM company c
                 JOIN user_roles ur ON c.company_id = ur.company_id
                 JOIN roles r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId} 
        ORDER BY c.company_id DESC
        LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @ResultMap("companyMapper")
    List<CompaniesDTO> getAllCompany(Integer offset, Integer limit, Integer userId);
    @Select("""
        SELECT
            DISTINCT c.company_id,
            c.company_name,
            c.profile_image,
            c.cover_image,
            c.description,
            c.created_at,
            c.email,
            c.address,
            c.phone,
            r.role_name
        FROM company c
                 JOIN user_roles ur ON c.company_id = ur.company_id
                 JOIN roles r ON ur.role_id = r.role_id
        WHERE ur.user_id = #{userId} AND r.role_id = 1
        ORDER BY c.company_id DESC
        LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @ResultMap("companyMapper")
    List<CompaniesDTO> getAllOwnCompanyByUserId(Integer offset, Integer limit, Integer userId);

    @Select("""
        SELECT DISTINCT p.*,c.company_name
        FROM project p
                 JOIN user_roles ur ON p.project_id = ur.project_id
                 JOIN company c ON c.company_id = ur.company_id
        WHERE ur.user_id = #{userId}
        ORDER BY p.project_id ASC
        LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @Results(id = "projectResult", value = {
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "companyName", column = "company_name")
    })
    List<ProjectsDTO> getAllProjectByUserId(Integer offset, Integer limit, Integer userId);

    @Select("""
            SELECT
                DISTINCT t.task_id,
                         t.task_name,
                         t.status,
                         t.phase_id,
                         t.created_at,
                         t.due_date,
                         t.attachment,
                         ur.company_id,
                         c.company_name
            FROM
                task t
                    JOIN
                user_roles ur ON t.task_id = ur.task_id
                    JOIN
                company c ON ur.company_id = c.company_id
            WHERE
                ur.user_id = #{userId}
            ORDER BY
                t.created_at DESC
            LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};                             
    """)
    @Results(id = "taskMapper", value = {
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskStatus", column = "status"),
            @Result(property = "dueDate", column = "due_date"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "attachment", column = "attachment"),
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name")
    })
    List<TasksDTO> getAllTasksByUserId(Integer offset, Integer limit, Integer userId);

    @Select("""
            SELECT
                DISTINCT r.report_id,
                     r.description,
                 c.company_name,
             ph.phase_id,
                 ph.price,
                 r.location,
                 r.problem,
                 u.user_id,
                 u.first_name,
                 u.last_name,
                 u.avatar,
                 u.email,
                 r.created_at
             FROM
                 company c
                     JOIN
                 project p ON c.company_id = p.company_id
                     JOIN
                 phases ph ON p.project_id = ph.project_id
                     JOIN
                 report r ON ph.phase_id = r.phase_id
                     JOIN
                 users u ON r.user_id = u.user_id
             WHERE
                 c.company_id = #{companyId}
             ORDER BY r.report_id DESC
         LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @Results(id = "reportMapper", value = {
            @Result(property = "reportId", column = "report_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "location", column = "location"),
            @Result(property = "problem", column = "problem"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "email", column = "email"),
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "price", column = "price")
    })
    List<ReportsDTO> getAllReportsByUserId(Integer offset, Integer limit, Integer companyId);

    @Select("""
            SELECT
                DISTINCT a.apply_id,
                         c.company_name,
                         pr.title,
                         u.user_id,
                         u.first_name,
                         u.last_name,
                         a.created_at
            FROM
                users u
                    JOIN
                apply a ON u.user_id = a.user_id
                    JOIN
                apply_post_recruitment apr ON a.apply_id = apr.apply_id
                    JOIN
                post_recruitment pr ON apr.post_recruitment_id = pr.post_recruitment_id
                    JOIN
                company c ON pr.company_id = c.company_id
            WHERE
                c.company_id = #{companyId}
            ORDER BY
                a.apply_id DESC
            LIMIT #{limit} OFFSET (#{offset}-1) * #{limit};
    """)
    @Results(id = "appliesMapper", value = {
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "fileAttachment", column = "file_attachment"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_Name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "title", column = "title"),
            @Result(property = "companyName", column = "company_name"),

    })
    List<AppliesDTO> getAllApplyByCompanyId(Integer offset, Integer limit, Integer companyId);

    @Select("""
        SELECT
            p.phase_id,
            p.phase_name,
            COUNT(CASE WHEN t.status = 'not yet' THEN 1 END) AS not_yet,
            COUNT(CASE WHEN t.status = 'on progress' THEN 1 END) AS on_progress,
            COUNT(CASE WHEN t.status = 'completed' THEN 1 END) AS completed
        FROM
            public.phases p
                LEFT JOIN
            public.task t ON p.phase_id = t.phase_id
        WHERE
            p.phase_id = #{phaseId}
        GROUP BY
            p.phase_id, p.phase_name;
    """)
    @Results(id = "countTaskStatusMapper", value = {
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "phaseName", column = "phase_name"),
            @Result(property = "notYet", column = "not_yet"),
            @Result(property = "onProgress", column = "on_progress"),
            @Result(property = "completed", column = "completed")
    })
    CountTaskStatusDTO countTaskStatusByPhaseId(Integer phaseId);
}
