package me.kverna.roger.server.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import me.kverna.roger.server.data.User;

/**
 * Utility class for dealing with password hashing and comparisons.
 * This uses BCrypt to hash and salt passwords.
 */
public class PasswordUtil {

    /**
     * Encrypts the given password using BCrypt.
     *
     * @param password the password to encrypt
     * @return the encrypted password
     */
    public static String encrypt(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    /**
     * Encrypts the given password and compares it to the given
     * user's password. Returns true if the passwords are equal.
     *
     * @param password an unencrypted password
     * @param user     the user to compare
     * @return true if the given password matches the user's password.
     */
    public static boolean verify(String password, User user) {
        return BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified;
    }
}
