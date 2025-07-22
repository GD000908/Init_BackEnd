package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignupDto {
    private String userId;
    private String name;
    private String password;
    private String confirmPassword;
    private String phone;
    private String email;
    private List<String> interests;
    private String jobTitle;   // 희망 직무
}