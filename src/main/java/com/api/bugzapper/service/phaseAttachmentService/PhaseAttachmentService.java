package com.api.bugzapper.service.phaseAttachmentService;

import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.entity.PhaseAttachment;
import com.api.bugzapper.model.request.PhaseAttachmentRequest;
import com.api.bugzapper.repository.PhaseAttachmentRepository;
import com.api.bugzapper.service.phaseService.PhaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhaseAttachmentService{
    private final PhaseAttachmentRepository phaseAttachmentRepository;
    private final PhaseService phaseService;
    public PhaseAttachmentService(PhaseAttachmentRepository phaseAttachmentRepository, PhaseService phaseService) {
        this.phaseAttachmentRepository = phaseAttachmentRepository;
        this.phaseService = phaseService;
    }
    public PhaseAttachment create(PhaseAttachmentRequest phaseAttachmentRequest) {
        phaseService.verifyCanManagePhase(phaseAttachmentRequest.getPhaseId());
        return phaseAttachmentRepository.create(phaseAttachmentRequest);
    }
    public PhaseAttachment findById(Integer id) {
        PhaseAttachment response = phaseAttachmentRepository.findById(id);
        if (response == null){
            throw new CustomNotFoundException("Phase attachment with id "+ id+ " not found.");
        }
        // Enforces the same private-phase membership check as viewing the phase directly.
        phaseService.findById(response.getPhase().getId());
        return response;
    }
    public PhaseAttachment updateAttachment(Integer id, PhaseAttachmentRequest phaseAttachmentRequest) {
        PhaseAttachment existing = phaseAttachmentRepository.findById(id);
        if (existing == null){
            throw new CustomNotFoundException("Phase attachment with id "+ id+ " not found.");
        }
        phaseService.verifyCanManagePhase(existing.getPhase().getId());
        return phaseAttachmentRepository.updatePhaseAttachment(id,phaseAttachmentRequest);
    }
    public void deleteAttachment(Integer id) {
        PhaseAttachment existing = phaseAttachmentRepository.findById(id);
        if (existing == null){
            throw new CustomNotFoundException("Phase attachment with id "+ id+ " not found.");
        }
        phaseService.verifyCanManagePhase(existing.getPhase().getId());
        phaseAttachmentRepository.deletePhaseAttachment(id);
    }
}
