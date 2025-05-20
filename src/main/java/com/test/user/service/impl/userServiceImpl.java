package com.test.user.service.impl;

import com.test.user.dto.RegisterRequest;
import com.test.user.dto.UserDto;
import com.test.user.entity.User;
import com.test.user.exception.EmailAlreadyExistsException;
import com.test.user.repository.UserRepository;
import com.test.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class userServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public userServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public User createTeamLeader(RegisterRequest request, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != User.Role.MANAGER) {
            throw new RuntimeException("Only managers can create team leaders");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        User teamLeader = new User();
        teamLeader.setName(request.getName());
        teamLeader.setEmail(request.getEmail());
        teamLeader.setPassword(passwordEncoder.encode(request.getPassword()));
        teamLeader.setRole(User.Role.TEAM_LEADER);
        teamLeader.setLeader(manager);

        return userRepository.save(teamLeader);
    }

    @Override
    public User createTeamHelpDesk(RegisterRequest request, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != User.Role.MANAGER) {
            throw new RuntimeException("Only managers can create team help desks");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        User teamHelpDesk = new User();
        teamHelpDesk.setName(request.getName());
        teamHelpDesk.setEmail(request.getEmail());
        teamHelpDesk.setPassword(passwordEncoder.encode(request.getPassword()));
        teamHelpDesk.setRole(User.Role.HELP_DESK);
        teamHelpDesk.setLeader(manager);

        System.out.println("teamHelpDesk: " + teamHelpDesk);
        return userRepository.save(teamHelpDesk);
    }

    @Override
    public User createEmployee(RegisterRequest request, UUID leaderId) {
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new RuntimeException("Team leader not found"));

        if (leader.getRole() != User.Role.TEAM_LEADER) {
            throw new RuntimeException("Only team leaders can create employees");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        User employee = new User();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setRole(User.Role.EMPLOYEE);
        employee.setLeader(leader);

        return userRepository.save(employee);
    }

    @Override
    public List<UserDto> getTeamLeadersByManager(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != User.Role.MANAGER) {
            throw new RuntimeException("User is not a manager");
        }

        return userRepository.findByLeaderId(managerId).stream()
                .filter(user -> user.getRole() == User.Role.TEAM_LEADER)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getEmployeesByLeader(UUID leaderId) {
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new RuntimeException("Team leader not found"));

        if (leader.getRole() != User.Role.TEAM_LEADER) {
            throw new RuntimeException("User is not a team leader");
        }

        return userRepository.findByLeaderId(leaderId).stream()
                .filter(user -> user.getRole() == User.Role.EMPLOYEE)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserDto mapToDto(User user) {
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
