package com.pagepal.capstone.dtos.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStaffResponse {
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
}
