package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.HistoryDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.request.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AppUserRepository {

    @Select("""       
            INSERT INTO users (first_name, last_name, email, password, gender, dob, created_at, type, is_verified)
            VALUES (#{user.firstName}, #{user.lastName}, #{user.email}, #{user.password}, #{user.gender}, #{user.dob}, CURRENT_TIMESTAMP, true, false)
            RETURNING *
            """)
    @Results(id = "userMap", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dob", column = "dob"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "type", column = "type"),
            @Result(property = "isVerified", column = "is_verified")
    })
    AppUser saveLocalUser(@Param("user") AppUserRequest appUserRequest);

    @Select("""
            INSERT INTO users (first_name, last_name, email, gender, dob, created_at, type, is_verified)
            VALUES (#{user.firstName}, #{user.lastName}, #{user.email}, #{user.gender}, #{user.dob}, CURRENT_TIMESTAMP, false, true)
            RETURNING *
            """)
    @Results(id = "googleMap", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dob", column = "dob"),
            @Result(property = "email", column = "email"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "type", column = "type"),
            @Result(property = "isVerified", column = "is_verified"),
    })
    AppUser saveUserWithGoogle(@Param("user") GoogleRequest googleRequest);

    @Select("""
             SELECT * FROM users WHERE email = #{email}
            """)
    @ResultMap("userMap")
    AppUser findUserByEmail(@Param("email") String email);

    @Select("""
            SELECT * FROM users
            WHERE user_id = #{userId}
            """)
    @Results(id = "userMapper", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dob", column = "dob"),
            @Result(property = "email", column = "email"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isVerified", column = "is_verified"),
    })
    AppUserDTO getUserById(Integer userId);

    @Select("""
            SELECT * FROM users
            WHERE user_id = #{userId}
            """)
    @Results(id = "userDTOMapper", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dob", column = "dob"),
            @Result(property = "email", column = "email"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "isVerified", column = "is_verified"),
    })
    AppUserDTO getUserDtoById(Integer userId);

    @Update("""
            UPDATE users SET password = #{request.password} WHERE email = #{email}
            """)
    void resetPasswordByEmail(@Param("request") PasswordRequest passwordRequest, @Param("email") String email);

    @Update("""
            UPDATE users SET password = #{request.password} WHERE email = #{email}
            """)
    void forgetPassword(@Param("request") ForgetPasswordRequest forgetPasswordRequest, @Param("email") String email);

    @Update("""
            UPDATE users SET avatar = #{avatar} WHERE email = #{email}
            """)
    void insertUserProfile(String avatar, String email);

    @Update("""
            UPDATE users SET experience = CAST(#{experience} AS jsonb) WHERE email = #{email}
            """)
    void insertUserExperience(@Param("experience") String experience, @Param("email") String email);
    @Update("""
        UPDATE users SET experience = null WHERE email = #{email}
    """)
    void deleteUserExperience(String email);

    @Select("""
        UPDATE users
        SET first_name = #{user.firstName}, last_name = #{user.lastName}, gender = #{user.gender}, dob = #{user.dob},
        updated_at = CURRENT_TIMESTAMP, bio = #{user.bio}
        WHERE email = #{email}
        RETURNING *
    """)
    @ResultMap("userMap")
    AppUser updateUser(@Param("user") UpdateUserRequest updateUserRequest, String email);

    @Delete("""
        DELETE FROM users WHERE email = #{email}
    """)
    @ResultMap("userMap")
    void deleteUserByEmail(String email);

    @Select("""
            SELECT * FROM users
            WHERE user_id = #{userId}
            """)
    @ResultMap("userMap")
    AppUser findById(Integer userId);

    @Select("""
            SELECT
                 raf.rate_and_feedback_id,
                 c.company_id,
                 c.company_name,
                 c.profile_image,
                 ph.phase_id,
                 ph.phase_name,
                 ph.description AS phase_description,
                 prj.project_id,
                 raf.feedback,
                 raf.rate_value,
                 raf.created_at
             FROM
                 report rep
                     LEFT JOIN rate_and_feedback raf ON raf.rate_and_feedback_id = rep.rate_and_feedback_id
                     JOIN users u ON rep.user_id = u.user_id
                     JOIN phases ph ON rep.phase_id = ph.phase_id
                     JOIN project prj ON ph.project_id = prj.project_id
                     LEFT JOIN company c ON c.company_id = prj.company_id OR c.company_id = raf.company_id
             WHERE
                 raf.type = true
                OR (u.user_id = #{userId} AND ph.is_private = false)
             ORDER BY
                 raf.created_at,
                 raf.rate_and_feedback_id,
                 rep.created_at DESC         
            LIMIT
                #{limit} OFFSET (#{offset} - 1) * #{limit};                                                 
    """)
    @Results(id = "historyMapper", value = {
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfileImage", column = "profile_image"),
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "phaseName", column = "phase_name"),
            @Result(property = "phaseDescription", column = "phase_description"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "rateFeedbackId", column = "rate_and_feedback_id"),
            @Result(property = "feedback", column = "feedback"),
            @Result(property = "rateValue", column = "rate_value"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<HistoryDTO> getAllHistoryByUserId(Integer offset, Integer limit, Integer userId);

    @Select("""
        SELECT ur.user_id
        FROM task t
        JOIN user_roles ur ON t.task_id = ur.task_id
        WHERE t.task_id = #{taskId};
        
    """)
    Integer getUserIdByTaskId(Integer taskId);

    @Select("""
        SELECT avatar FROM users users
        WHERE user_id = #{userId}
    """)
    String getAvatarByUserId(Integer userId);

    @Update("""
        UPDATE users SET is_verified = true WHERE email = #{email}
    """)
    void setIsVerifiedToTrue(@Param("email") String email);

    @Delete("""
        DELETE FROM rate_and_feedback WHERE rate_and_feedback_id = #{rateAndFeedbackId}
    """)
    void deleteHistoryByRateAndFeedbackId(@Param("rateAndFeedbackId") Integer id);
}
