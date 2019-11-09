package me.kverna.roger.server.service;

import me.kverna.roger.server.data.User;
import me.kverna.roger.server.repository.UserRepository;
import me.kverna.roger.server.security.JwtManager;
import me.kverna.roger.server.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository repository;
    private JwtManager jwtManager;

    @Autowired
    public UserService(UserRepository repository, JwtManager jwtManager) {
        this.repository = repository;
        this.jwtManager = jwtManager;
    }

    /**
     * Create a user if no user with the same email exists.
     *
     * @param user the user to create
     * @return the user that is created
     */
    public User createUser(User user) {
        User existingUser = repository.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        return repository.save(user);
    }

    /**
     * Find a user by id and return it.
     *
     * @param id the id of the user
     * @return user if found
     */
    public Optional<User> getUser(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        return user;
    }

    /**
     * Find a user by email and return it.
     *
     * @param email the email of the user
     * @return user if found
     */
    public User getUser(String email) {
        User user = repository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return user;
    }

    /**
     * Delete a user from the repository.
     *
     * @param user the user to delete
     */
    public void deleteUser(User user) {
        repository.delete(user);
    }

    /**
     * Verify password given for the user and return a new JSON web token.
     *
     * @param user     the user to login to
     * @param password the password of the account to login to
     * @return a new JSON web token
     */
    public String login(User user, String password) {
        // Verify the given password
        if (!PasswordUtil.verify(password, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Generate the JSON web token for the user and send it back
        return jwtManager.generateToken(user.getEmail());
    }
}
