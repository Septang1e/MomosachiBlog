package com.septangle.momosachiblog.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserQueryDTO {
    private Long userId;

    private String nickname;

    private String username;

    /**
     * 0 代表管理员，1代表普通用户
     */
    private Integer isAdmin;

    private String email;

    // 0为confirmed, 1为未验证, 2为验证不通过
    private Integer emailStatus;

}
