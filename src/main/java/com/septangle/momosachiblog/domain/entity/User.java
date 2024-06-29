package com.septangle.momosachiblog.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.septangle.momosachiblog.constant.UserConstant;
import com.septangle.momosachiblog.domain.dto.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    //-1为匿名
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String nickname;

    private String username;

    private String password;

    private String website;

    private String avatar;

    private String email;

    // 0为confirmed, 1为未验证, 2为验证不通过
    private Integer emailStatus;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    // 0为 register
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    private Integer isDelete;

    // 0 为普通用户，1 为管理员
    private Integer isAdmin;

    public User(CommentDTO commentDTO) {
        this.password = UserConstant.FAKE_USER_PASSWORD;
        this.username = UserConstant.FAKE_USER_USERNAME;

        this.nickname = commentDTO.getNickname();
        this.website = commentDTO.getWebsite();
        this.email = commentDTO.getEmail();

        if(!commentDTO.getAvatar().equals("not-update")){
            this.avatar = (commentDTO.getAvatar());
        }else{
            this.avatar = commentDTO.getAvatarRandom();
        }

        this.isAdmin = 0;
    }

}
