package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.PostRecruitmentNewsfeed;
import com.api.bugzapper.model.entity.PostRecruitment;
import com.api.bugzapper.model.request.PostRecruitmentRequest;
import com.api.bugzapper.model.request.PostRecruitmentRequestForUpdate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostRecruitmentRepository {
    @Select("""
                INSERT INTO post_recruitment(description, title, fee, image,created_at, post_data, company_id, application_title, role_id, project_id)
                VALUES (#{post.description}, #{post.title},#{post.fee},#{post.image},CURRENT_TIMESTAMP,
                    CAST(#{postJson} AS jsonb), #{post.companyId}, #{post.applicationTitle}, COALESCE(#{post.roleId}, 4), #{post.projectId})
                RETURNING post_recruitment_id
            """)
    @Results(id = "postMapper", value = {
            @Result(property = "postRecruitmentId", column = "post_recruitment_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "title", column = "title"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "image", column = "image"),
            @Result(property = "applicationTitle", column = "application_title"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "postData", column = "post_data"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "projectId", column = "project_id"),
    })
    Integer createPostRecruitment(@Param("post") PostRecruitmentRequest postRecruitmentRequest, @Param("postJson") String postRecruitmentJson);

    @Select("""
                    SELECT
                        post.*,
                        JSON_AGG(ap.apply_id) AS apply_ids,
                        company.company_id,
                        company.company_name,
                        company.profile_image,
                        company.description AS company_description,
                        proj.project_name
                    FROM
                        post_recruitment post
                            LEFT JOIN
                        apply_post_recruitment ap
                            ON
                        post.post_recruitment_id = ap.post_recruitment_id
                            LEFT JOIN
                        company company
                            ON
                        post.company_id = company.company_id
                            LEFT JOIN
                        project proj
                            ON
                        post.project_id = proj.project_id
                    WHERE
                        post.post_recruitment_id = #{id}
                    GROUP BY
                        post.post_recruitment_id,
                        company.company_id,
                        proj.project_name;
            """)
    @Results(id = "postMapApply", value = {
            @Result(property = "postRecruitmentId", column = "post_recruitment_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "title", column = "title"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "image", column = "image"),
            @Result(property = "applicationTitle", column = "application_title"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "companyDescription", column = "company_description"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "postData", column = "post_data"),
            @Result(property = "userRoleId", column = "user_role_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "applies", column = "post_recruitment_id",
                    many = @Many(select = "com.api.bugzapper.repository.ApplyPostRecruitmentRepository.findApplyIdByPostRecruitmentId"))
    })
    PostRecruitment getPostRecruitmentById(Integer id);

    @Delete("""
                DELETE FROM post_recruitment WHERE post_recruitment_id = #{id}
            """)
    @ResultMap("postMapper")
    void deletePostRecruitmentById(Integer id);

    @Select("""
                SELECT
                    post.*,
                    JSON_AGG(ap.apply_id) AS apply_ids,
                    'recruitment' AS type,
                    c.company_id,
                    c.company_name,
                    c.profile_image,
                    c.description AS company_description,
                    proj.project_name
                FROM
                    post_recruitment post
                       LEFT JOIN
                   apply_post_recruitment ap ON post.post_recruitment_id = ap.post_recruitment_id
                       LEFT JOIN
                   company c ON post.company_id = c.company_id
                       LEFT JOIN
                   project proj ON post.project_id = proj.project_id
                GROUP BY
                    post.post_recruitment_id,c.company_id,proj.project_name
                ORDER BY
                    post.post_recruitment_id DESC
                LIMIT #{limit} OFFSET (#{offset} - 1) * #{limit}
            """)
    @Results(id = "postNewsfeedMap", value = {
            @Result(property = "postRecruitmentId", column = "post_recruitment_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "title", column = "title"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "image", column = "image"),
            @Result(property = "applicationTitle", column = "application_title"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "postData", column = "post_data"),
            @Result(property = "userRoleId", column = "user_role_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "companyDescription", column = "company_description"),
            @Result(property = "applies", column = "post_recruitment_id",
                    many = @Many(select = "com.api.bugzapper.repository.ApplyPostRecruitmentRepository.findApplyIdByPostRecruitmentId"))
    })
    List<PostRecruitmentNewsfeed> getAllPostRecruitment(Integer offset, Integer limit);

    @Select("""
                UPDATE post_recruitment SET
                description = #{post.description}, title = #{post.title}, fee = #{post.fee}, image = #{post.image},
                updated_at = CURRENT_TIMESTAMP, post_data = CAST(#{postJson} AS jsonb), application_title = #{post.applicationTitle},
                role_id = COALESCE(#{post.roleId}, role_id), status = COALESCE(#{post.status}, status),
                project_id = COALESCE(#{post.projectId}, project_id)
                WHERE post_recruitment_id = #{id}
                RETURNING post_recruitment_id
            """)
    @ResultMap("postMapper")
    Integer updatePostRecruitmentById(@Param("post") PostRecruitmentRequestForUpdate postRecruitmentRequest, @Param("postJson") String postRecruitmentJson, Integer id);

    @Update("""
                UPDATE post_recruitment SET status = #{status}, updated_at = CURRENT_TIMESTAMP
                WHERE post_recruitment_id = #{id}
            """)
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Select("""
                SELECT
                    post.*,
                    JSON_AGG(ap.apply_id) AS apply_ids,
                    'recruitment' AS type,
                    c.company_id,
                    c.company_name,
                    c.profile_image,
                    c.description AS company_description,
                    proj.project_name
                FROM
                    post_recruitment post
                       LEFT JOIN
                   apply_post_recruitment ap ON post.post_recruitment_id = ap.post_recruitment_id
                       LEFT JOIN
                   company c ON post.company_id = c.company_id
                       LEFT JOIN
                   project proj ON post.project_id = proj.project_id
                WHERE
                    c.company_id = #{companyId}
                GROUP BY
                    post.post_recruitment_id, c.company_id, proj.project_name
                ORDER BY
                    post.post_recruitment_id DESC
                LIMIT #{limit} OFFSET (#{offset} - 1) * #{limit}
            """)
    @ResultMap("postNewsfeedMap")
    List<PostRecruitmentNewsfeed> getAllPostRecruitmentByCompanyId(Integer companyId, Integer offset, Integer limit);

}
