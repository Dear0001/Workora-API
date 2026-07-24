package com.api.bugzapper.repository;

import com.api.bugzapper.model.dto.OtpsDTO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OtpRepository {

    @Insert("""
                INSERT INTO otps(code, issued_at, expiration, verify, user_id)
                VALUES(#{otpsDTO.optCode}, #{otpsDTO.issuedDate}, #{otpsDTO.expiration}, #{otpsDTO.verify}, #{otpsDTO.userId})
            """)

    void saveOtp(@Param("otpsDTO") OtpsDTO otpsDTO);

    /** Exact match (varchar code column, or driver maps int to string). */
    @Select("""
                SELECT *
                FROM otps
                WHERE code = #{code}
                LIMIT 1
            """)
    @Results(id = "otpMap", value = {
            @Result(property = "optCode", column = "code"),
            @Result(property = "issuedDate", column = "issued_at"),
            @Result(property = "expiration", column = "expiration"),
            @Result(property = "verify", column = "verify"),
            @Result(property = "userId", column = "user_id")
    })
    OtpsDTO getOtpByExactCode(@Param("code") String code);

    /**
     * Fallback when {@code code} is stored as integer (leading zeros lost) or formatting differs:
     * compare both sides as 6-digit strings.
     */
    @Select("""
                SELECT *
                FROM otps
                WHERE lpad(TRIM(BOTH FROM code::text), 6, '0') = lpad(TRIM(BOTH FROM #{code}), 6, '0')
                LIMIT 1
            """)
    @ResultMap("otpMap")
    OtpsDTO findOtpByCodeNormalized(@Param("code") String code);

    @Update("""
                UPDATE otps
                SET verify = #{otpsDTO.verify}
                WHERE code = #{otpsDTO.optCode}
            """)
    void updateOtp(@Param("otpsDTO") OtpsDTO otpsDTO);
}
