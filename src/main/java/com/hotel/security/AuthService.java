package com.hotel.security;

import com.hotel.model.AdminUser;
import com.hotel.repository.AdminUserRepository;

import java.util.Optional;

public class AuthService {

    private final AdminUserRepository adminUserRepository;

    public AuthService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    public Optional<AdminUser> login(String username, String password) {
        // TODO Final: replace plaintext comparison with BCrypt.checkpw(password, hash).
        return adminUserRepository.findByUsername(username)
                .filter(admin -> admin.getPasswordHash().equals(password));
    }
}
