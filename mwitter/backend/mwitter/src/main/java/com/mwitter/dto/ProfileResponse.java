package com.mwitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private String id;

    private String username;

    private String email;

    private int followersCount;

    private int followingCount;
}
