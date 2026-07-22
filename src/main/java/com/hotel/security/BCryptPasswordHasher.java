package com.hotel.security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHasher {

    public String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public boolean verify(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
