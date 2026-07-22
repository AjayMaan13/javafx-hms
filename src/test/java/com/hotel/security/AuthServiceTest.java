package com.hotel.security;

import com.hotel.model.AdminUser;
import com.hotel.model.enums.Role;
import com.hotel.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void loginSucceedsForACorrectPasswordAgainstAStoredBCryptHashAndFailsOtherwise() {
        AdminUserRepository adminUserRepository = new AdminUserRepository();
        BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();
        AuthService authService = new AuthService(adminUserRepository, passwordHasher);

        String username = "auth-test-" + UUID.randomUUID();
        AdminUser adminUser = new AdminUser(username, passwordHasher.hash("correct-password"), Role.ADMIN);
        adminUserRepository.save(adminUser);

        Optional<AdminUser> success = authService.login(username, "correct-password");
        Optional<AdminUser> wrongPassword = authService.login(username, "wrong-password");
        Optional<AdminUser> unknownUser = authService.login("no-such-user", "correct-password");

        assertTrue(success.isPresent());
        assertEquals(adminUser.getId(), success.get().getId());
        assertFalse(wrongPassword.isPresent());
        assertFalse(unknownUser.isPresent());
    }
}
