package org.example.appsmallcrm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.UserCreateDTO;
import org.example.appsmallcrm.entity.embeddable.Role;
import org.example.appsmallcrm.repo.UserRepository;
import org.example.appsmallcrm.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // If directly encoding password
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // If you were setting password directly

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            UserCreateDTO adminUser = new UserCreateDTO();
            adminUser.setUsername("admin");
            adminUser.setPassword("Admin@12345"); // Choose a strong default password
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@example.com");
            adminUser.setRole(Role.ROLE_ADMIN);
            
            userService.createUser(adminUser);
            log.info("Default admin user 'admin' created.");
        } else {
            log.info("Admin user 'admin' already exists.");
        }
    }
}