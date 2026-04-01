package com.finance.config;


import com.finance.entity.ERole;
import com.finance.entity.Role;
import com.finance.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_VIEWER));
            roleRepository.save(new Role(ERole.ROLE_ANALYST));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
    }
}
