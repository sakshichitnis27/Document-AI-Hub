package com.chitnis.document_management_app.controller;
import com.chitnis.document_management_app.entity.User;
import com.chitnis.document_management_app.service.JwtService;
import com.chitnis.document_management_app.service.UserService;
import com.chitnis.document_management_app.dto.AuthResponse;
import com.chitnis.document_management_app.dto.AuthRequest;

import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequest request) {
        try {
            User user = userService.register(request.getEmail(), request.getPassword());
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
        } catch (EntityExistsException ex) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid credentials"));
        }
    }
}
