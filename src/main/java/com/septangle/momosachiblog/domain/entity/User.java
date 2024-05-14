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

    private Integer isAdmin;

    public void setUserByCommentDTO(CommentDTO commentInfo) {
        this.password = UserConstant.nonePassword;
        this.username = UserConstant.noneUsername;

        this.nickname = commentInfo.getNickname();
        this.website = commentInfo.getWebsite();
        this.email = commentInfo.getEmail();

        if(!commentInfo.getAvatar().equals("not-update")){
            this.avatar = (commentInfo.getAvatar());
        }else{
            this.avatar = commentInfo.getAvatarRandom();
        }

        this.setIsAdmin(0);
    }

}
