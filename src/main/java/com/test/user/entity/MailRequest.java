package com.test.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mail_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailRequest {

    public enum Status {
        HD_REQ,
        HD_ACCEPT,
        HD_REJECT,
        MANAGER_ACCEPT,
        MANAGER_REJECT,
        TL_ACCEPT,
        TL_REJECT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_leader_id")
    private User teamLeader;

    private Boolean managerApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
