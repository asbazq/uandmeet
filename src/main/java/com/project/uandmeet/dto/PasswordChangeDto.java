package com.project.uandmeet.dto;

import lombok.Getter;

@Getter
public class PasswordChangeDto {
    private String username;
    private String passwordCheck;
    private String newPassword;
    private String newPasswordCheck;
}
