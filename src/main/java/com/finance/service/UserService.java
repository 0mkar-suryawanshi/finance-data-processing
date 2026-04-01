package com.finance.service;


import com.finance.entity.ERole;
import com.finance.entity.Role;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.payload.request.SignupRequest;
import com.finance.repository.RoleRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role defaultRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            roles.add(defaultRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Admin role not found"));
                        roles.add(adminRole);
                        break;
                    case "analyst":
                        Role analystRole = roleRepository.findByName(ERole.ROLE_ANALYST)
                                .orElseThrow(() -> new RuntimeException("Analyst role not found"));
                        roles.add(analystRole);
                        break;
                    default:
                        Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                                .orElseThrow(() -> new RuntimeException("Viewer role not found"));
                        roles.add(viewerRole);
                }
            });
        }

        user.setRoles(roles);
        user.setActive(true);
        userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Additional user management methods (update, delete, activate/deactivate) can be added
}
