package com.test.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.test.user.entity.MailRequest;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestApprovalDto {

    @NotNull(message = "Request ID is required")
    private UUID requestId;

    private Boolean approved;

    private MailRequest.Status status;

    private String comments;
}
