package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.CompanyRatingDTO;
import com.api.bugzapper.model.dto.TopCompany;
import com.api.bugzapper.model.entity.Company;
import com.api.bugzapper.model.request.CompanyRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompanyRepository {

    @Results(id = "companyMapper", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "inviteCode", column = "invite_code"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "deletedAt", column = "deleted_at"),
    })
    @Select("""
                INSERT INTO company (company_name,email,phone,address,description,profile_image,cover_image,invite_code,created_at)
                VALUES(#{company.name},#{company.email},#{company.phone},#{company.address},#{company.description},#{company.profileImage},
                #{company.coverImage},#{code},current_timestamp) RETURNING *
            """)
    Company createCompany(@Param("company") CompanyRequest companyRequest, String code);


    @Select("""
                SELECT * FROM company WHERE company.company_id = #{id}
            """)
    @ResultMap("companyMapper")
    Company getCompanyById(Integer id);

    @Select("""
                SELECT * FROM company WHERE company.company_id = #{id}
            """)
    @Results(id = "companyMapperForRateAndFeedback", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
    })
    Company getCompanyByIdForRateAndFeedback(Integer id);

    @ResultMap("companyMapper")
    @Select("""
                UPDATE company SET company_name = #{company.name},
                                    email = #{company.email},
                                    phone = #{company.phone},
                                    address = #{company.address},
                                    description = #{company.description},
                                    profile_image = #{company.profileImage},
                                    cover_image = #{company.coverImage},
                                    updated_at = current_timestamp
                WHERE company_id = #{id} RETURNING *
            """)
    Company updateCompany(Integer id, @Param("company") CompanyRequest companyRequest);

    @Delete("""
                DELETE FROM company WHERE company_id = #{id}
            """)
    void deleteCompany(Integer id);

    @Update("""
            UPDATE company SET cover_image = #{fileName} WHERE company_id = #{id}
            """)
    void insertCompanyCover(String fileName, Integer id);

    @Update("""
            UPDATE company SET profile_image = #{fileName} WHERE company_id = #{id}
            """)
    void insertCompanyProfile(String fileName, Integer id);

    @ResultMap("companyMapper")
    @Select("""
            SELECT * FROM company WHERE invite_code = #{code}
            """)
    Company findByCode(String code);

    @Select("""
            SELECT COUNT(*) FROM user_roles
            WHERE user_id = #{userId} AND company_id = #{companyId} AND role_id = 1
            """)
    int isOwnerOfCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("""
             SELECT
                 COUNT(*)
             FROM
                 users u
                     JOIN
                 user_roles ur ON u.user_id = ur.user_id
             WHERE
                 u.user_id = #{userId}
               AND ur.company_id = #{companyId}
               AND ur.project_id = #{projectId}
               AND ur.phase_id = #{phaseId}
               AND ur.role_id IN (1,2);
            """)
    Integer isTrue(Integer userId, Integer companyId, Integer projectId, Integer phaseId);

    @Select("""
            SELECT company.company_id
            FROM phases
            JOIN
                project ON phases.project_id = project.project_id
            JOIN
                company ON project.company_id = company.company_id
            WHERE
            phases.phase_id = #{phaseId};
    """)
    Integer getCompanyIdByPhaseId(@Param("phaseId") Integer phaseId);

    @Select("""
            SELECT  c.company_id,
                    c.company_name,
                    c.profile_image,
                    c.description,
                    SUM(rf.rate_value) AS total_rate_value
            FROM 
                rate_and_feedback rf
            JOIN 
                company c ON c.company_id = rf.company_id AND rf.type = false
            GROUP BY 
                c.company_id, c.company_name, c.profile_image
           HAVING
                SUM(rf.rate_value) > 0
           ORDER BY total_rate_value DESC
           LIMIT 5;
    """)
    @Results(id = "topCompanyMapper", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "totalRateValue", column = "total_rate_value"),
            @Result(property = "description", column = "description")
    })
    List<TopCompany> getTopCompany();

    @Select("""
            
         SELECT  c.company_id,
                 c.company_name,
                 c.profile_image,
                 c.cover_image,
                 c.description,
                 c.created_at,
                 c.updated_at,
                 c.email,
                 c.phone,
                 c.address,
                 COALESCE(SUM(rf.rate_value), 0) AS total_rate_value,
                 COALESCE(COUNT(rf.user_id), 0) AS number_of_ratings,
                 COALESCE((SUM(rf.rate_value)::FLOAT / NULLIF(COUNT(rf.user_id), 0)), 0) AS average_rating
         FROM
             company c
                 LEFT JOIN
             rate_and_feedback rf ON c.company_id = rf.company_id AND rf.type = false
         WHERE c.company_id = #{companyId}
         GROUP BY
             c.company_id, c.company_name, c.profile_image, c.cover_image, c.description, c.created_at, c.updated_at, c.email, c.phone, c.address;
    """)
    @Results(id = "CompanyRatingMapper", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "rating", column = "average_rating"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "email", column = "email"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "address", column = "address")
    })
    CompanyRatingDTO getCompanyByIdWithRating(@Param("companyId") Integer companyId);

    @Select("""
        SELECT c.* FROM company c
        JOIN project p on c.company_id = p.company_id
        WHERE p.project_id = #{projectId}
    """)
    @ResultMap("companyMapper")
    Company getCompanyByProjectId(Integer projectId);
}
