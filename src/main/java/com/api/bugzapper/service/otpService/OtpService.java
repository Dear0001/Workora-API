package com.api.bugzapper.service.otpService;

import com.api.bugzapper.model.dto.OtpsDTO;
import com.api.bugzapper.repository.OtpRepository;
import com.api.bugzapper.util.OtpUtil;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public void saveOtp(OtpsDTO otpsDTO) {
        otpRepository.saveOtp(otpsDTO);
    }

    /**
     * Resolves an OTP row by trying exact code, normalized 6-digit form, and padded text match.
     * Never throws for null input — returns null if not found.
     */
    public OtpsDTO getOtp(String otp) {
        if (otp == null || otp.isBlank()) {
            return null;
        }
        String trimmed = otp.trim();

        OtpsDTO row = otpRepository.getOtpByExactCode(trimmed);
        if (row != null) {
            return row;
        }

        String normalized = OtpUtil.normalizeOtpDigits(trimmed);
        if (!normalized.isEmpty() && !normalized.equals(trimmed)) {
            row = otpRepository.getOtpByExactCode(normalized);
            if (row != null) {
                return row;
            }
        }

        if (!normalized.isEmpty()) {
            row = otpRepository.findOtpByCodeNormalized(normalized);
            if (row != null) {
                return row;
            }
        }

        return otpRepository.findOtpByCodeNormalized(trimmed);
    }

    public void updateOtp(OtpsDTO otpsDTO) {
        otpRepository.updateOtp(otpsDTO);
    }
}
