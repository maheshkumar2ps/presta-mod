package com.prestashop.service;

import com.prestashop.dto.auth.LoginRequest;
import com.prestashop.dto.auth.LoginResponse;
import com.prestashop.entity.Employee;
import com.prestashop.exception.AuthenticationException;
import com.prestashop.repository.EmployeeRepository;
import com.prestashop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(employee);
        long expiresIn = jwtTokenProvider.getExpiration();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .employee(LoginResponse.EmployeeDto.builder()
                        .id(employee.getId())
                        .email(employee.getEmail())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .fullName(employee.getFullName())
                        .profile(employee.getProfile().getName())
                        .build())
                .build();
    }

    public Employee getCurrentEmployee(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Employee not found"));
    }
}
