package com.test.user.service.impl;

import com.test.user.dto.MailRequestApprovalDto;
import com.test.user.dto.MailRequestDto;
import com.test.user.entity.MailRequest;
import com.test.user.entity.User;
import com.test.user.repository.MailRequestRepository;
import com.test.user.repository.UserRepository;
import com.test.user.service.MailRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MailRequestServiceImpl implements MailRequestService {

    private final MailRequestRepository mailRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public MailRequestServiceImpl(MailRequestRepository mailRequestRepository, UserRepository userRepository) {
        this.mailRequestRepository = mailRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public MailRequestDto createMailRequest(MailRequestDto requestDto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.EMPLOYEE) {
            throw new RuntimeException("Only employees can create mail requests");
        }

        MailRequest mailRequest = new MailRequest();
        mailRequest.setSubject(requestDto.getSubject());
        mailRequest.setBody(requestDto.getBody());
        mailRequest.setUser(user);
        mailRequest.setManagerApproved(null); // Pending status
        mailRequest.setStatus(MailRequest.Status.HD_REQ); // Initial status is help desk request

        MailRequest savedRequest = mailRequestRepository.save(mailRequest);
        return mapToDto(savedRequest);
    }

    @Override
    public List<MailRequestDto> getUserMailRequests(UUID userId) {
        return mailRequestRepository.findByUserId(userId).stream()
                .filter(data -> data.getStatus() != MailRequest.Status.HD_REQ
                        && data.getStatus() != MailRequest.Status.MANAGER_REJECT)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MailRequestDto> getPendingRequestsForManager(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != User.Role.MANAGER && manager.getRole() != User.Role.TEAM_LEADER) {
            throw new RuntimeException("Only managers or team leaders can review mail requests");
        }

        return mailRequestRepository.findByUser_LeaderId(managerId).stream()
                .filter(request -> request.getManagerApproved() == null) // Only pending requests
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MailRequestDto> getPendingRequestsActionRequired(UUID managerId) {
        return mailRequestRepository.findByUser_LeaderId(managerId).stream()
                .filter(request -> request.getStatus() == MailRequest.Status.MANAGER_REJECT
                        || request.getStatus() == MailRequest.Status.HD_REQ)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MailRequestDto reviewMailRequest(MailRequestApprovalDto approvalDto, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != User.Role.MANAGER && manager.getRole() != User.Role.TEAM_LEADER
                && manager.getRole() != User.Role.HELP_DESK) {
            throw new RuntimeException("Only managers or team leaders can review mail requests");
        }

        MailRequest mailRequest = mailRequestRepository.findById(approvalDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Mail request not found"));

        // Verify the request belongs to a user under this manager
        // if (!mailRequest.getUser().getLeader().getId().equals(managerId)) {
        // throw new RuntimeException("You can only review requests from your team
        // members");
        // }

        mailRequest.setManager(manager);
        if (approvalDto.getApproved()) {
            mailRequest.setManagerApproved(true);
        } else if (approvalDto.getApproved() == false) {
            mailRequest.setManagerApproved(false);
        } else {
            mailRequest.setManagerApproved(null);
        }
        mailRequest.setComments(approvalDto.getComments());
        mailRequest.setReviewedAt(LocalDateTime.now());

        // Set appropriate status based on approver's role and decision
        if (manager.getRole() == User.Role.MANAGER) {
            if (approvalDto.getApproved()) {
                mailRequest.setStatus(MailRequest.Status.MANAGER_ACCEPT);
            } else {
                mailRequest.setStatus(MailRequest.Status.MANAGER_REJECT);
            }
        } else if (manager.getRole() == User.Role.TEAM_LEADER) {
            if (approvalDto.getApproved()) {
                mailRequest.setStatus(MailRequest.Status.TL_ACCEPT);
            } else {
                mailRequest.setStatus(MailRequest.Status.TL_REJECT);
            }
        } else if (manager.getRole() == User.Role.HELP_DESK) {
            if (approvalDto.getApproved()) {
                mailRequest.setStatus(MailRequest.Status.HD_ACCEPT);
            } else {
                mailRequest.setStatus(MailRequest.Status.HD_REJECT);
            }
        }

        MailRequest updatedRequest = mailRequestRepository.save(mailRequest);
        return mapToDto(updatedRequest);
    }

    @Override
    public MailRequestDto reviewMailRequestHelpDesk(MailRequestApprovalDto approvalDto, UUID managerId) {
        User helpDesk = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Help desk not found"));

        if (helpDesk.getRole() != User.Role.HELP_DESK) {
            throw new RuntimeException("Only help desks can review mail requests");
        }

        MailRequest mailRequest = mailRequestRepository.findById(approvalDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Mail request not found"));

        mailRequest.setManager(helpDesk);
        mailRequest.setComments(approvalDto.getComments());
        mailRequest.setReviewedAt(LocalDateTime.now());

        Boolean approved = approvalDto.getApproved();
        // Set help desk specific status
        if (Boolean.TRUE.equals(approved)) {
            mailRequest.setStatus(MailRequest.Status.HD_ACCEPT);
        } else if (Boolean.FALSE.equals(approved)) {
            mailRequest.setStatus(MailRequest.Status.HD_REJECT);
        } else {
            mailRequest.setStatus(MailRequest.Status.HD_REQ);
        }

        MailRequest updatedRequest = mailRequestRepository.save(mailRequest);
        return mapToDto(updatedRequest);
    }

    @Override
    public List<MailRequestDto> getAllApprovedRequests() {
        return mailRequestRepository.findByManagerApproved(true).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MailRequestDto> getAllPenreqAft() {
        return mailRequestRepository.findByManagerApproved(null).stream()
                .filter(data -> data.getCreatedAt().plusMinutes(1).isBefore(LocalDateTime.now()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MailRequestDto> getAllPenreqAftHelpDesk() {
        return mailRequestRepository.findByManagerApproved(null).stream()
                .filter(data -> data.getCreatedAt().plusMinutes(2).isBefore(LocalDateTime.now()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MailRequestDto getMailRequestById(UUID id) {
        MailRequest mailRequest = mailRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mail request not found"));
        return mapToDto(mailRequest);
    }

    private MailRequestDto mapToDto(MailRequest mailRequest) {
        MailRequestDto dto = new MailRequestDto();
        dto.setId(mailRequest.getId());
        dto.setSubject(mailRequest.getSubject());
        dto.setBody(mailRequest.getBody());
        if (mailRequest.getManagerApproved() != null) {
            dto.setManagerApproved(mailRequest.getManagerApproved());
        }
        dto.setComments(mailRequest.getComments());
        dto.setCreatedAt(mailRequest.getCreatedAt());
        dto.setReviewedAt(mailRequest.getReviewedAt());
        dto.setStatus(mailRequest.getStatus());

        // User information
        User user = mailRequest.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        }

        // Manager information
        User manager = mailRequest.getManager();
        System.out.println("manager: " + manager);
        if (manager != null) {
            dto.setManagerId(manager.getId());
            dto.setManagerName(manager.getName());
            dto.setManagerRole(manager.getRole());
        }

        return dto;
    }
}
