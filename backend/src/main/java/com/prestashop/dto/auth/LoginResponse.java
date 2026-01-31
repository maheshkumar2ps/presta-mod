package com.prestashop.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private EmployeeDto employee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String profile;
    }
}
