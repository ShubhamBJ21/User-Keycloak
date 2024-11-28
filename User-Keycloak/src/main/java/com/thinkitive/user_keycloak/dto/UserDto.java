package com.thinkitive.user_keycloak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String id;

    @NotBlank(message = "firstname is mandatory")
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    private String firtName;

    @NotBlank(message = "lastname is mandatory")
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 25, message = "Name must be between 2 and 25 characters")
    private String userName;

    @NotBlank(message = "password is mandatory")
    @Size(min = 8, max = 25, message = "Name must be between 8 and 25 characters")
    private String password;
}
