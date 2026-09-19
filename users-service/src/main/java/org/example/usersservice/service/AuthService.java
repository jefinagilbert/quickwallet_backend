package org.example.usersservice.service;


import org.example.usersservice.dto.AuthRequest;
import org.example.usersservice.dto.AuthResponse;
import org.example.usersservice.entity.User;
import org.example.usersservice.repository.UserRepository;
import org.example.usersservice.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;
    public final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<String> register(AuthRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email Already Exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        User ans = userRepository.save(user);

        return ResponseEntity.ok(ans.getEmail()+ " is registered successfully");
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Email not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token);
    }

}
