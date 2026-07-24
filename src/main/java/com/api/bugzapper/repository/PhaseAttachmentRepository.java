package com.api.bugzapper.repository;

import com.api.bugzapper.model.entity.PhaseAttachment;
import com.api.bugzapper.model.request.PhaseAttachmentRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PhaseAttachmentRepository {
    @Results(id = "attachmentMapper", value = {
            @Result(property = "id", column = "phase_attachment_id"),
            @Result(property = "phase", column = "phase_id",
                    one = @One(select = "com.api.bugzapper.repository.PhaseRepository.findById")
            )
    })
    @Select("""
                INSERT INTO phase_attachment (phase_id,attachment) VALUES (#{phaseAttachment.phaseId},
                #{phaseAttachment.attachment}) RETURNING *
            """)
    PhaseAttachment create(@Param("phaseAttachment") PhaseAttachmentRequest phaseAttachmentRequest);

    @ResultMap("attachmentMapper")
    @Select("""
                SELECT * FROM phase_attachment WHERE phase_attachment_id = #{id}
            """)
    PhaseAttachment findById(Integer id);

    @Select("""
                UPDATE phase_attachment SET phase_id = #{phaseAttachment.phaseId}, attachment = #{phaseAttachment.attachment}
                WHERE phase_attachment_id = #{id} RETURNING *
            """)
    @ResultMap("attachmentMapper")
    PhaseAttachment updatePhaseAttachment(Integer id, @Param("phaseAttachment") PhaseAttachmentRequest phaseAttachmentRequest);

    @Delete("""
                DELETE FROM phase_attachment WHERE phase_attachment_id = #{id}
            """)
    void deletePhaseAttachment(Integer id);
}
