package com.septangle.momosachiblog.module.rabbit;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckModule {
    private Long userId;

    private String email;

}
