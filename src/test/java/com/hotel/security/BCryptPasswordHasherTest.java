package com.hotel.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashIsNeverTheSameAsThePlainTextPassword() {
        String hash = hasher.hash("admin123");

        assertNotEquals("admin123", hash);
    }

    @Test
    void verifySucceedsForTheCorrectPasswordAndFailsForTheWrongOne() {
        String hash = hasher.hash("admin123");

        assertTrue(hasher.verify("admin123", hash));
        assertFalse(hasher.verify("wrong-password", hash));
    }
}
