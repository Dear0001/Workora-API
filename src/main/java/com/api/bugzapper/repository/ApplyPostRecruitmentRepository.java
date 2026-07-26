package com.api.bugzapper.repository;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ApplyPostRecruitmentRepository {

    @Insert("""
        INSERT INTO apply_post_recruitment(post_recruitment_id, apply_id, created_at)
        VALUES (#{postRecruitmentId}, #{applyId}, CURRENT_TIMESTAMP)
    """)
    @Results(id = "mapper", value = {
            @Result(property = "applyPostRecruitmentId", column = "id"),
            @Result(property = "postRecruitmentId", column = "post_recruitment_id"),
            @Result(property = "applyId", column = "apply_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    void insertToApplyPostRecruitment(Integer applyId, Integer postRecruitmentId);

    @Select("""
        SELECT apply_id FROM apply_post_recruitment WHERE post_recruitment_id = #{id}
    """)
    @ResultType(Integer.class)
    Integer findApplyIdByPostRecruitmentId(Integer id);

    @Select("""
        SELECT post_recruitment_id FROM apply_post_recruitment WHERE apply_id = #{id}
    """)
    @ResultType(Integer.class)
    Integer findPostRecruitmentIdByApplyId(Integer applyId);

    @Delete("""
        DELETE FROM apply_post_recruitment WHERE apply_id = #{applyId}
    """)
    void deleteApplyPostRecruitmentByApplyId(Integer applyId);
}
