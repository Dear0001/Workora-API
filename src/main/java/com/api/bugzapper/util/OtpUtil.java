package com.api.bugzapper.util;

import com.api.bugzapper.model.dto.OtpsDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class OtpUtil {

    /**
     * Aligns user input with stored 6-digit codes (leading zeros, e.g. 001234).
     */
    public static String normalizeOtpDigits(String otp) {
        if (otp == null) {
            return "";
        }
        String digits = otp.trim().replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return "";
        }
        if (digits.length() > 6) {
            return digits;
        }
        try {
            return String.format("%06d", Integer.parseInt(digits));
        } catch (NumberFormatException e) {
            return digits;
        }
    }

    public OtpsDTO generateOTP(Integer userId) {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);

        while (output.length() < 6) {
            output = "0" + output;
        }

        LocalDateTime issuedDate = LocalDateTime.now();
        LocalDateTime expiration = issuedDate.plusMinutes(10);
        return new OtpsDTO(output, issuedDate, expiration, false,userId);
    }
}
