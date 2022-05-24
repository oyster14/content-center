package com.guanshi.contentcenter;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LombokTest {
    public static void main(String[] args) {
        UserRegisterDTO build = UserRegisterDTO.builder()
                .email("hahahha")
                .agreement(true)
                .password("oyster")
                .confirmPassword("oyster")
                .mobile("123412")
                .build();
        log.info("沟造出来的UserRegisterDTO = {}", build);
    }
}

@Data
@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@RequiredArgsConstructor
class UserRegisterDTO {
    private String email;
    private String password;
    private String confirmPassword;
    private String mobile;
    private boolean agreement;

}
