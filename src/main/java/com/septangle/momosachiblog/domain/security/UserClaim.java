package com.septangle.momosachiblog.domain.security;

import com.septangle.momosachiblog.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClaim {

    private Long userId;

    private String userRole;

    private Long timestamp;
}
