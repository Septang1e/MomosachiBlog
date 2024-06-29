package com.septangle.momosachiblog.module.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailSendModule {

    private String address;

    private String content;

}
