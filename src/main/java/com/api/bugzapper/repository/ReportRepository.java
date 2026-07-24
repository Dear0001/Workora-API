package com.api.bugzapper.repository;

import com.api.bugzapper.model.entity.Report;
import com.api.bugzapper.model.request.ReportPhaseRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportRepository {
    @Select("""
                SELECT * FROM report WHERE report_id = #{id}
            """)
    @Results(id = "reportMapper", value = {
            @Result(property = "reportId", column = "report_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "problem", column = "problem"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "userId", column = "user_id",
                    one = @One(select = "com.api.bugzapper.repository.AppUserRepository.getUserDtoById")),
            @Result(property = "phaseId", column = "phase_id",
                    one = @One(select = "com.api.bugzapper.repository.PhaseRepository.findById")),
            @Result(property = "taskId", column = "task_id",
                    one = @One(select = "com.api.bugzapper.repository.TaskRepository.getTaskById"))
    })
    Report getReportById(Integer id);


    @Select("""
                INSERT INTO report(description,location,problem,created_at,user_id,phase_id)
                VALUES (#{report.description}, #{report.location}, #{report.problem}, CURRENT_TIMESTAMP, 
                #{userId}, #{report.phaseId})
            """)
    Integer createReportPhase(@Param("report") ReportPhaseRequest reportPhaseRequest,@Param("userId") Integer userId);

    @Select("""
        SELECT * FROM report WHERE phase_id = #{phaseId} LIMIT #{limit} OFFSET (#{offset}-1) * #{limit} 
    """)
    @ResultMap("reportMapper")
    List<Report> getReportByPhaseId(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("phaseId") Integer phaseId);
}
