package com.test.user.controller;

import com.test.user.dto.MailRequestApprovalDto;
import com.test.user.dto.MailRequestDto;
import com.test.user.security.JwtTokenUtil;
import com.test.user.service.MailRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mail-requests")
public class MailRequestController {

    private final MailRequestService mailRequestService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public MailRequestController(MailRequestService mailRequestService, JwtTokenUtil jwtTokenUtil) {
        this.mailRequestService = mailRequestService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // Employee endpoints

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<MailRequestDto> createMailRequest(
            @Valid @RequestBody MailRequestDto requestDto,
            @RequestHeader("Authorization") String token) {

        UUID userId = jwtTokenUtil.extractUserId(token.substring(7));
        MailRequestDto createdRequest = mailRequestService.createMailRequest(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getMyRequests(
            @RequestHeader("Authorization") String token) {

        UUID userId = jwtTokenUtil.extractUserId(token.substring(7));
        List<MailRequestDto> requests = mailRequestService.getUserMailRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my-requests/with-comments")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getMyRequestsWithComments(
            @RequestHeader("Authorization") String token) {

        UUID userId = jwtTokenUtil.extractUserId(token.substring(7));
        List<MailRequestDto> requests = mailRequestService.getUserMailRequests(userId);
        // No additional filtering needed as the service already returns only the user's
        // requests
        return ResponseEntity.ok(requests);
    }

    // Manager endpoints

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getPendingRequests(
            @RequestHeader("Authorization") String token) {

        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        List<MailRequestDto> pendingRequests = mailRequestService.getPendingRequestsForManager(managerId);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/pending/action-required")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getPendingRequestsActionRequired(
            @RequestHeader("Authorization") String token) {

        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        List<MailRequestDto> pendingRequests = mailRequestService.getPendingRequestsActionRequired(managerId);
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'MANAGER','HELP_DESK')")
    public ResponseEntity<MailRequestDto> reviewMailRequest(
            @Valid @RequestBody MailRequestApprovalDto approvalDto,
            @RequestHeader("Authorization") String token) {

        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        MailRequestDto reviewedRequest = mailRequestService.reviewMailRequest(approvalDto, managerId);
        return ResponseEntity.ok(reviewedRequest);
    }

    @PostMapping("/help-desk/review")
    @PreAuthorize("hasAnyRole('HELP_DESK')")
    public ResponseEntity<MailRequestDto> reviewMailRequestHelpDesk(
            @Valid @RequestBody MailRequestApprovalDto approvalDto,
            @RequestHeader("Authorization") String token) {

        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        MailRequestDto reviewedRequest = mailRequestService.reviewMailRequestHelpDesk(approvalDto, managerId);
        return ResponseEntity.ok(reviewedRequest);
    }

    // Admin endpoints

    @GetMapping("/approved")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getAllApprovedRequests() {
        List<MailRequestDto> approvedRequests = mailRequestService.getAllApprovedRequests();
        return ResponseEntity.ok(approvedRequests);
    }

    @GetMapping("/leader/pending-approval")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MailRequestDto>> getAllPendingRequestsAft() {
        List<MailRequestDto> approvedRequests = mailRequestService.getAllPenreqAft();
        return ResponseEntity.ok(approvedRequests);
    }

    @GetMapping("/help-desk/pending-approval")
    @PreAuthorize("hasRole('HELP_DESK')")
    public ResponseEntity<List<MailRequestDto>> getAllPendingRequestsAftHelpDesk() {
        List<MailRequestDto> approvedRequests = mailRequestService.getAllPenreqAftHelpDesk();
        return ResponseEntity.ok(approvedRequests);
    }

    // Common endpoints

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<MailRequestDto> getMailRequestById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {

        // Additional authorization check could be added here
        MailRequestDto mailRequest = mailRequestService.getMailRequestById(id);
        return ResponseEntity.ok(mailRequest);
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TEAM_LEADER', 'MANAGER')")
    public ResponseEntity<MailRequestDto> getMailRequestWithComments(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {

        UUID userId = jwtTokenUtil.extractUserId(token.substring(7));
        MailRequestDto mailRequest = mailRequestService.getMailRequestById(id);

        // Check if the user is authorized to view this request
        // Either the user created it, or is the manager who reviewed it, or is a
        // manager above
        if (!mailRequest.getUserId().equals(userId) &&
                (mailRequest.getManagerId() == null || !mailRequest.getManagerId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mailRequest);
    }
}
