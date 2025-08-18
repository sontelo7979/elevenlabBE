package com.example.demo.model;

import lombok.*;

@Getter
@AllArgsConstructor
public enum ERole {
    ROLE_ADMIN(1),
    ROLE_STAFF(2),
    ROLE_CUSTOMER(3);

    private final int value;


}