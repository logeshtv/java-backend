package com.test.user.service;

import com.test.user.dto.MailRequestApprovalDto;
import com.test.user.dto.MailRequestDto;

import java.util.List;
import java.util.UUID;

public interface MailRequestService {

    // Employee operations
    MailRequestDto createMailRequest(MailRequestDto requestDto, UUID userId);

    List<MailRequestDto> getUserMailRequests(UUID userId);

    // Manager operations
    List<MailRequestDto> getPendingRequestsForManager(UUID managerId);

    List<MailRequestDto> getPendingRequestsActionRequired(UUID managerId);

    MailRequestDto reviewMailRequest(MailRequestApprovalDto approvalDto, UUID managerId);

    MailRequestDto reviewMailRequestHelpDesk(MailRequestApprovalDto approvalDto, UUID managerId);

    // Admin operations
    List<MailRequestDto> getAllApprovedRequests();

    List<MailRequestDto> getAllPenreqAft();

    List<MailRequestDto> getAllPenreqAftHelpDesk();

    // Common operations
    MailRequestDto getMailRequestById(UUID id);
}
