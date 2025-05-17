package com.test.user.service;

import com.test.user.dto.RegisterRequest;
import com.test.user.dto.UserDto;
import com.test.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // Authentication
    User registerUser(RegisterRequest request);
    User findByEmail(String email);
    
    // User management by role
    User createTeamLeader(RegisterRequest request, UUID managerId);
    User createEmployee(RegisterRequest request, UUID leaderId);
    
    // User retrieval
    List<UserDto> getTeamLeadersByManager(UUID managerId);
    List<UserDto> getEmployeesByLeader(UUID leaderId);
    
    // User operations
    UserDto getUserById(UUID id);
    void deleteUser(UUID id);
}
