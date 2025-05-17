package com.test.user.dto;

import com.test.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
    
    @NotBlank(message = "Password is required")
    // Simplified pattern that will accept Password1!
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$", 
             message = "Password must be at least 8 characters and include a number, uppercase, lowercase, and special character")
    private String password;
    
    // Role is set by the system based on who's creating the user
    private User.Role role;
}
