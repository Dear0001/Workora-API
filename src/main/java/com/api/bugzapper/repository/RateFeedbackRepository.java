package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.RateFeedbackFromCompanyToUser;
import com.api.bugzapper.model.entity.RateFeedback;
import com.api.bugzapper.model.entity.RateFeedbackFromUser;
import com.api.bugzapper.model.entity.RateFeedbackToCompany;
import com.api.bugzapper.model.request.RateFeedbackCompanyToUserRequest;
import com.api.bugzapper.model.request.RateFeedbackUserToCompanyRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RateFeedbackRepository {

    @Results(id = "rateFeedbackMapper", value = {
            @Result(property = "id", column = "rate_and_feedback_id"),
            @Result(property = "rateValue", column = "rate_value"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getUserById")),
            @Result(property = "company", column = "company_id",
                    one = @One(select = "com.api.bugzapper.repository.CompanyRepository.getCompanyByIdForRateAndFeedback"))
    })
    @Select("""
                INSERT INTO rate_and_feedback(feedback, rate_value, user_id, company_id, type,created_at)
                VALUES (#{rate.feedback},#{rate.rateValue},#{userId},#{rate.companyId},false,current_timestamp)
                RETURNING *
            """)
    RateFeedback userRateFeedbackToCompany(@Param("rate") RateFeedbackUserToCompanyRequest request, @Param("userId") Integer userId);

    @Select("""
                SELECT *FROM rate_and_feedback WHERE rate_and_feedback_id = #{id}
            """)
    @ResultMap("rateFeedbackMapper")
    RateFeedback findById(Integer id);

    @Select("""
                INSERT INTO rate_and_feedback(feedback, rate_value, user_id, company_id,created_at, type)
                        VALUES (#{request.feedback},#{request.rateValue},#{userId},#{request.companyId},current_timestamp, true)
                        RETURNING *
            """)
    @ResultMap("rateFeedbackMapper")
    RateFeedback companyRateFeedbackToUser(@Param("request") RateFeedbackCompanyToUserRequest request, Integer userId);

    @Select("""
               SELECT
                   COUNT(r.user_id) AS totalCompanyRated,
                   SUM(r.rate_value) AS totalRateValue
               FROM
                   rate_and_feedback r
               WHERE
                   r.user_id = #{userId}
                 AND r.type = TRUE
               GROUP BY
                   r.user_id;
            """)
    RateFeedbackToCompany getRateAndFeedbackByUserId(Integer companyId);

    @Select("""
        SELECT COUNT(company_id) FROM rate_and_feedback 
        WHERE company_id = #{companyId} AND user_id = #{userId} AND type = true
    """)
    Integer checkIfIsRate(Integer userId, Integer companyId);

    @Select("""
        SELECT COUNT(user_id) FROM rate_and_feedback 
        WHERE company_id = #{companyId} AND user_id = #{userId} AND type = false
    """)
    Integer checkIfIsRateIsTrue(Integer userId, Integer companyId);

    @Select("""
        SELECT * FROM rate_and_feedback WHERE company_id = #{companyId} AND type = false;
    """)
    @Results(id = "rateAndFeedBack", value = {
            @Result(property = "id", column = "rate_and_feedback_id"),
            @Result(property = "rateValue", column = "rate_value"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getUserById")),
    })
    List<RateFeedbackFromUser> getAllRateFeedbackOfCompany(Integer companyId);

    @Select("""
        SELECT * FROM rate_and_feedback WHERE user_id = #{userId} AND type = true;
    """)
    @Results(id = "rateFeedback", value = {
            @Result(property = "id", column = "rate_and_feedback_id"),
            @Result(property = "rateValue", column = "rate_value"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "company", column = "company_id",
                    one = @One(select = "com.api.bugzapper.repository.CompanyRepository.getCompanyByIdForRateAndFeedback"))
    })
    List<RateFeedbackFromCompanyToUser> getAllRateFeedbackOfCompanyToUser(Integer userId);
}
