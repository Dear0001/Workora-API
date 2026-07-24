package com.api.bugzapper.repository;

import com.api.bugzapper.model.entity.Apply;
import com.api.bugzapper.model.entity.ApplyCompany;
import com.api.bugzapper.model.request.ApplyRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ApplyRepository {
    @Select("""
                INSERT INTO apply( user_id, created_at, apply_data)
                VALUES (#{userId}, CURRENT_TIMESTAMP, CAST(#{applyJson} AS jsonb))
                RETURNING *
            """)
    @Results(id = "applyMapper", value = {
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "applyData", column = "apply_data"),
            @Result(property = "status", column = "status"),
            @Result(property = "userId", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getUserDtoById"))
    })
    Apply createApply(String applyJson, @Param("apply") ApplyRequest applyRequest,@Param("userId") Integer userId);

    @Select("""
            SELECT a.*, apr.post_recruitment_id
            FROM apply a
            LEFT JOIN apply_post_recruitment apr ON a.apply_id = apr.apply_id
            WHERE a.apply_id = #{id};
            """)
    @Results(id = "applyMapPost", value = {
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "applyData", column = "apply_data"),
            @Result(property = "status", column = "status"),
            @Result(property = "userId", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getUserDtoById")),
            @Result(property = "postRecruitmentId", column = "apply_id",
                    many = @Many(select = "com.api.bugzapper.repository.ApplyPostRecruitmentRepository.findPostRecruitmentIdByApplyId"))
    })
    Apply getApplyById(Integer id);

    @Update("""
                UPDATE apply SET status = #{status} WHERE apply_id = #{id}
            """)
    void updateApplyStatus(@Param("id") Integer id, @Param("status") String status);

    @Select("""
        SELECT
            ar.apply_post_recruitment_id
        FROM
            apply a
                JOIN
            apply_post_recruitment ar ON a.apply_id = ar.apply_id
        WHERE
            a.user_id = #{userId} AND ar.post_recruitment_id = #{postRecruitmentId}
        LIMIT 1;
    """)
    Integer getApplyPostRecruitmentId(Integer userId, Integer postRecruitmentId);

    @Select("""
        SELECT a.*, pr.title, first_name, last_name, company_name
        FROM apply a
                 JOIN users u ON a.user_id = u.user_id
                 JOIN apply_post_recruitment apr ON a.apply_id = apr.apply_id
                 JOIN post_recruitment pr ON apr.post_recruitment_id = pr.post_recruitment_id
                 JOIN user_roles ur ON u.user_id = ur.user_id
                 JOIN company c ON ur.company_id = c.company_id
        WHERE c.company_id = #{companyId};
    """)
    @Results(id = "getApplyMapper", value = {
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "applyData", column = "apply_data"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<ApplyCompany> getApplyByCompanyId(Integer companyId);
}
