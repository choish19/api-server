package com.nas.manager.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nas.manager.dto.LoginRequest;
import com.nas.manager.dto.SignupRequest;
import com.nas.manager.model.Setting;
import com.nas.manager.model.User;
import com.nas.manager.repository.UserRepository;
import com.nas.manager.security.JwtTokenProvider;
import com.nas.manager.util.LogUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LogUtil.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public ResponseEntity<?> signup(SignupRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("Username is already taken");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email is already in use");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(Collections.singleton("ROLE_USER"));
            user.setSetting(new Setting());

            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            throw e;
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }

            String token = tokenProvider.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("로그인 중 오류 발생", e);
            throw e;
        }
    }
}