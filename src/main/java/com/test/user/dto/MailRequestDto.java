package com.test.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.test.user.entity.MailRequest;
import com.test.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailRequestDto {

    private UUID id;

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(max = 2000, message = "Body must be less than 2000 characters")
    private String body;

    private UUID userId;
    private String userName;
    private String userEmail;

    private UUID managerId;
    private String managerName;
    private User.Role managerRole;

    private Boolean managerApproved;
    private String comments;

    private MailRequest.Status status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
