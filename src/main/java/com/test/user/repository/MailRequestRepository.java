package com.test.user.repository;

import com.test.user.entity.MailRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MailRequestRepository extends JpaRepository<MailRequest, UUID> {
    
    // Find requests by user ID
    List<MailRequest> findByUserId(UUID userId);
    
    // Find requests by manager ID
    List<MailRequest> findByManagerId(UUID managerId);
    
    // Find requests by user's leader ID (for manager to review)
    List<MailRequest> findByUser_LeaderId(UUID leaderId);
    
    // Find all approved requests
    List<MailRequest> findByManagerApproved(Boolean approved);
    
    // Find approved requests by manager ID
    List<MailRequest> findByManagerIdAndManagerApproved(UUID managerId, Boolean approved);
}
