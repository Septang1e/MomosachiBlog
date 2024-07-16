package com.septangle.momosachiblog.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value = "email_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailLog {
    @TableId
    private Long id;

    // 标题
    private String subject;

    // 邮箱地址
    private String emailAddress;

    // 正文 ()
    private String content;

    // 0 send, 1 receive(directly)
    private Integer status;

    // 0 confirmed, 1为未验证, 2为验证不通过
    private Integer emailStatus;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
