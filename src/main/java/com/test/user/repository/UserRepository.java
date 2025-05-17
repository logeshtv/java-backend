package com.test.user.repository;

import com.test.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    List<User> findByLeaderId(UUID leaderId);
    
    List<User> findByRole(User.Role role);
    
    boolean existsByEmail(String email);
}
