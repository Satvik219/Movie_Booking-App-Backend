package org.satvik.moviebookingsystembackend.service;

import org.satvik.moviebookingsystembackend.dto.AuthDTO;
import org.satvik.moviebookingsystembackend.entity.User;
import org.satvik.moviebookingsystembackend.exception.ResourceAlreadyExistsException;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.UserRepository;
import org.satvik.moviebookingsystembackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    public void changePassword(String email, AuthDTO.ChangePasswordRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getCurrentPassword())
        );

        userRepository.updatePassword(email, passwordEncoder.encode(request.getNewPassword()));
    }

    private AuthDTO.AuthResponse buildAuthResponse(String token, User user) {
        AuthDTO.AuthResponse response = new AuthDTO.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }
}

