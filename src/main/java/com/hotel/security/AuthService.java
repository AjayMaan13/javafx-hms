package com.hotel.security;

import com.hotel.model.AdminUser;
import com.hotel.repository.AdminUserRepository;

import java.util.Optional;

public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final BCryptPasswordHasher passwordHasher;

    public AuthService(AdminUserRepository adminUserRepository, BCryptPasswordHasher passwordHasher) {
        this.adminUserRepository = adminUserRepository;
        this.passwordHasher = passwordHasher;
    }

    public Optional<AdminUser> login(String username, String password) {
        return adminUserRepository.findByUsername(username)
                .filter(admin -> passwordHasher.verify(password, admin.getPasswordHash()));
    }
}
