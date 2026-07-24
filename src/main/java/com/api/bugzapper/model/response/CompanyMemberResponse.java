package com.api.bugzapper.model.response;

import com.api.bugzapper.model.dto.CompanyMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyMemberResponse{

    private String message;
    private HttpStatus status;
    private int code;
    private int totalMembers;
    private List<CompanyMemberDTO> payload;
}
