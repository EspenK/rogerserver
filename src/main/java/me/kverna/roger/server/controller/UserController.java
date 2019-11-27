package me.kverna.roger.server.controller;

import me.kverna.roger.server.annotation.Authorized;
import me.kverna.roger.server.annotation.LoggedIn;
import me.kverna.roger.server.data.User;
import me.kverna.roger.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User newUser) {
        return userService.createUser(newUser);
    }

    @PostMapping("/authorize")
    public User login(@RequestBody User loginUser, HttpServletResponse response) {
        User user = userService.getUser(loginUser.getEmail());

        String token = userService.login(user, loginUser.getPassword());
        response.addHeader("Authorization", "Bearer " + token);

        return user;
    }

    @Authorized
    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Authorized
    @GetMapping("/current")
    public User currentUser(@LoggedIn User current) {
        return current;
    }

    @Authorized
    @DeleteMapping("/current")
    public void deleteUser(@LoggedIn User user) {
        userService.deleteUser(user);
    }
}
