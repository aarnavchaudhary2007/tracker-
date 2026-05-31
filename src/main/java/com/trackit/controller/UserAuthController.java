package com.trackit.controller;

import com.trackit.dto.AuthResponse;
import com.trackit.dto.SigninRequest;
import com.trackit.dto.SignupRequest;
import com.trackit.model.User;
import com.trackit.repository.ApplicationRepository;
import com.trackit.repository.UserRepository;
import com.trackit.service.JwtService;
import com.trackit.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Authentication", description = "Endpoints for user registration, login, and signout")
public class UserAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public UserAuthController(UserRepository userRepository, JwtService jwtService, TokenBlacklistService blacklistService, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.applicationRepository = applicationRepository;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username is already taken.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        User user = new User(request.getUsername(), hashedPassword);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully.");
        response.put("username", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signin")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid username or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }

    @PostMapping("/guest")
    @Operation(summary = "Authenticate a temporary guest session and issue JWT token")
    public ResponseEntity<?> signinGuest() {
        String guestUsername = "guest_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String dummyHashedPassword = BCrypt.hashpw(java.util.UUID.randomUUID().toString(), BCrypt.gensalt());
        User guestUser = new User(guestUsername, dummyHashedPassword);
        userRepository.save(guestUser);

        String token = jwtService.generateToken(guestUsername);
        return ResponseEntity.ok(new AuthResponse(token, guestUsername));
    }

    @PostMapping("/signout")
    @Operation(summary = "Invalidate user session / token")
    public ResponseEntity<Map<String, String>> signout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "Signout requested but no valid token header was found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = authHeader.substring(7).trim();
        blacklistService.blacklistToken(token);

        response.put("message", "Signed out successfully. Token invalidated.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account")
    @Operation(summary = "Permanently delete the user's account and purge their data")
    public ResponseEntity<Map<String, String>> deleteAccount(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("message", "To delete your account, a valid session token is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.substring(7).trim();
        String username;
        try {
            username = jwtService.validateToken(token);
        } catch (Exception e) {
            response.put("message", "We couldn't verify your session to perform account deletion.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (blacklistService.isBlacklisted(token)) {
            response.put("message", "Your session has already been invalidated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            response.put("message", "We couldn't locate an active account for your session.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Purge user application data (since single-tenant/local application data is owned by active user)
        applicationRepository.deleteAll();

        // Delete user account
        userRepository.delete(user);

        // Blacklist token
        blacklistService.blacklistToken(token);

        response.put("message", "Your account and tracking data have been permanently deleted.");
        return ResponseEntity.ok(response);
    }
}
