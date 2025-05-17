package com.test.user.controller;

import com.test.user.dto.RegisterRequest;
import com.test.user.dto.UserDto;
import com.test.user.entity.User;
import com.test.user.security.JwtTokenUtil;
import com.test.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/managers/leaders")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createTeamLeader(@Valid @RequestBody RegisterRequest request,
                                             @RequestHeader("Authorization") String token) {
        // Extract manager ID from JWT token
        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        
        User teamLeader = userService.createTeamLeader(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(teamLeader));
    }

    @PostMapping("/leaders/employees")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody RegisterRequest request,
                                           @RequestHeader("Authorization") String token) {
        // Extract leader ID from JWT token
        UUID leaderId = jwtTokenUtil.extractUserId(token.substring(7));
        
        User employee = userService.createEmployee(request, leaderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(employee));
    }

    @GetMapping("/managers/leaders")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getTeamLeaders(@RequestHeader("Authorization") String token) {
        UUID managerId = jwtTokenUtil.extractUserId(token.substring(7));
        return ResponseEntity.ok(userService.getTeamLeadersByManager(managerId));
    }

    @GetMapping("/leaders/employees")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<List<UserDto>> getEmployees(@RequestHeader("Authorization") String token) {
        UUID leaderId = jwtTokenUtil.extractUserId(token.substring(7));
        return ResponseEntity.ok(userService.getEmployeesByLeader(leaderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        
        if (user.getLeader() != null) {
            dto.setLeaderId(user.getLeader().getId());
        }
        
        return dto;
    }
}
