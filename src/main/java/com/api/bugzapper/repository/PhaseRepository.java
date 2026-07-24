package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.PhaseDTO;
import com.api.bugzapper.model.dto.PhaseNewsfeedDTO;
import com.api.bugzapper.model.entity.Phase;
import com.api.bugzapper.model.request.PhaseRequest;
import com.api.bugzapper.model.request.PublicPhaseRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PhaseRepository {

    @Select("""
                INSERT INTO phases (phase_name,description,price,image,project_id,link,created_at, is_private) 
                VALUES (#{phase.name},#{phase.description},#{phase.price},#{phase.image},#{phase.projectId},#{link},current_timestamp, true) RETURNING phase_id
            """)
    Integer createPrivatePhase(@Param("phase") PhaseRequest phaseRequest, String link);

    @Select("""
                SELECT * FROM phases WHERE phase_id = #{id}
            """)
    @Results(id = "phaseMapper", value = {
            @Result(property = "id", column = "phase_id"),
            @Result(property = "name", column = "phase_name"),
            @Result(property = "isPrivate", column = "is_private"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "price", column = "price"),
            @Result(property = "project", column = "project_id",
                    one = @One(select = "com.api.bugzapper.repository.ProjectRepository.getProjectById")
            )
    })
    Phase findById(Integer id);

    @Select("""
                UPDATE phases SET phase_name = #{phase.name},price= #{phase.price}, description = #{phase.description},
                project_id = #{phase.projectId}, image = #{phase.image},updated_at = current_timestamp, is_private = true
                WHERE phase_id = #{id} RETURNING *
            """)
    @ResultMap("phaseMapper")
    Phase updatePrivatePhase(Integer id, @Param("phase") PhaseRequest phaseRequest);

    @Delete("""
                DELETE FROM phases WHERE phase_id = #{id}
            """)
    void deletePhase(Integer id);

    @Select("""
                UPDATE phases SET link = #{link} WHERE phase_id = #{id} RETURNING *
            """)
    @ResultMap("phaseMapper")
    Phase updateLinkToEncrypt(String link, Integer id);

    @Select("""
                UPDATE phases SET is_private = #{isPrivate} WHERE phase_id = #{phaseId} RETURNING *
            """)
    @ResultMap("phaseMapper")
    Phase updatePhaseType(@Param("phaseId") Integer phaseId, @Param("isPrivate") Boolean isPrivate);

    @Select("""
                SELECT COUNT(*) FROM phases p
                JOIN project pr ON p.project_id = pr.project_id
                WHERE p.phase_id = #{phaseId} AND pr.company_id = #{companyId}
            """)
    int isPhaseInCompany(Integer phaseId, Integer companyId);

    @Select("""
                SELECT project_id FROM phases WHERE phase_id = #{phaseId}
            """)
    Integer findProjectIdByPhaseId(Integer phaseId);

    @Select("""
                SELECT * FROM phases WHERE phase_id = #{phaseId}
            """)
    @ResultMap("phaseMapper")
    Phase findByCode(@Param("phaseId") Integer link);

    @Select("""
                SELECT * FROM phases WHERE project_id = #{projectId}
            """)
    @ResultMap("phaseMapper")
    List<Phase> getAllPhaseByProjectId(Integer projectId);

    @Select("""
                SELECT
                     ph.phase_id,
                     ph.phase_name,
                     ph.description,
                     ph.price,
                     ph.image,
                     ph.project_id,
                     ph.is_private,
                     ph.link,
                     ph.created_at,
                     ph.updated_at,
                     co.company_id
                FROM
                    public.phases ph
                        JOIN
                    public.project pr ON ph.project_id = pr.project_id
                        JOIN
                    public.company co ON pr.company_id = co.company_id
                        JOIN
                    public.user_roles ur ON ph.phase_id = ur.phase_id
                        JOIN
                    public.users u ON ur.user_id = u.user_id
                WHERE
                    u.user_id = #{userId}
                ORDER BY co.company_id, ph.phase_id
                LIMIT #{limit} OFFSET (#{offset}-1) * #{limit}
            """)
    List<PhaseDTO> getAllPhaseByUserId(Integer userId, Integer offset, Integer limit);

    @Select("""
                SELECT
                      p.*,
                      'phase' AS type,
                      c.company_id,
                      c.company_name,
                      c.profile_image,
                      c.description AS company_description
                  FROM
                      phases p
                          LEFT JOIN
                      project pr ON p.project_id = pr.project_id
                          LEFT JOIN
                      company c ON pr.company_id = c.company_id
                  WHERE
                      p.is_private = false
                  ORDER BY
                      p.created_at DESC
                  LIMIT #{limit} OFFSET (#{offset} - 1) * #{limit};
            """)
    @Results(id = "publicPhaseMapper", value = {
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "phaseName", column = "phase_name"),
            @Result(property = "isPrivate", column = "is_private"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "companyProfile", column = "profile_image"),
            @Result(property = "companyDescription", column = "company_description")
    })
    List<PhaseNewsfeedDTO> getAllPublicPhase(Integer offset, Integer limit);

    @Select("""
                INSERT INTO phases (phase_name,description,price,image,project_id,link,created_at, is_private) 
                VALUES (#{phase.name},#{phase.description},#{phase.price},#{phase.image},#{phase.projectId},#{link},current_timestamp, false) RETURNING phase_id
            """)
    Integer createPublicPhase(@Param("phase") PublicPhaseRequest publicPhaseRequest, String link);

    @Select("""
                UPDATE phases SET phase_name = #{phase.name},price= #{phase.price}, description = #{phase.description},
                project_id = #{phase.projectId}, image = #{phase.image},updated_at = current_timestamp, is_private = false
                WHERE phase_id = #{id} RETURNING *
            """)
    @ResultMap("phaseMapper")
    Phase updatePublicPhase(Integer id,@Param("phase") PublicPhaseRequest publicPhaseRequest);

    @Select("""
                SELECT ph.phase_id FROM phases ph
                JOIN task t ON ph.phase_id = t.phase_id
                WHERE task_id = #{taskId}
            """)
    Integer getPhaseByTaskId(Integer taskId);
    @Select("""
        SELECT p.*
        FROM phases p
        JOIN project pr ON p.project_id = pr.project_id
        WHERE pr.company_id = #{companyId}
        AND p.is_private = false
        LIMIT #{limit} OFFSET (#{offset} - 1) * #{limit}
    """)
    @ResultMap("phaseMapper")
    List<PhaseNewsfeedDTO> getAllPublicPhasesByCompanyId(Integer companyId, Integer offset, Integer limit);

    @Select("""
        SELECT DISTINCT ON (ur.phase_id) p.*
        FROM user_roles ur
                 JOIN phases p ON ur.phase_id = p.phase_id
        WHERE ur.user_id = #{userId}
          AND ur.company_id = #{companyId}
          AND ur.project_id = #{projectId}
        ORDER BY ur.phase_id DESC;
    """)
    @ResultMap("phaseUserMapper")
    List<PhaseDTO> getAllPhaseByProjectIdAndUserId(Integer userId, Integer companyId, Integer projectId);

    @Select("""
            SELECT project_id FROM phases WHERE phase_id = #{phaseId}
            """)
    Integer getProjectIdByPhaseId(Integer phaseId);

    @Select("""
            SELECT phase_id FROM phases WHERE project_id = #{projectId}
            """)
    List<Integer> getPhaseIdsByProjectId(Integer projectId);

    @Select("""
            SELECT DISTINCT p.phase_id FROM phases p
            JOIN user_roles ur ON p.phase_id = ur.phase_id
            WHERE ur.user_id = #{userId} 
            AND ur.project_id = #{projectId}
            """)
    List<Integer> getPhaseIdsByUserIdAndProjectId(Integer userId, Integer projectId);

    @Select("""
                SELECT
                     ph.phase_id,
                     ph.phase_name,
                     ph.description,
                     ph.price,
                     ph.image,
                     ph.project_id,
                     ph.is_private,
                     ph.link,
                     ph.created_at,
                     ph.updated_at,
                     co.company_id
                FROM phases ph
                JOIN project pr ON ph.project_id = pr.project_id
                JOIN company co ON pr.company_id = co.company_id
                WHERE ph.project_id = #{projectId}
                ORDER BY ph.phase_id
            """)
    @Results(id = "phaseUserMapper", value = {
            @Result(property = "phaseId", column = "phase_id"),
            @Result(property = "phaseName", column = "phase_name"),
            @Result(property = "isPrivate", column = "is_private"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "companyId", column = "company_id"),
    })
    List<PhaseDTO> getAllPhaseDTOByProjectId(Integer projectId);
}
