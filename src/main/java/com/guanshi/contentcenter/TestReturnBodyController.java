package com.guanshi.contentcenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class TestReturnBodyController {
    @GetMapping("/test-hsr")
    public void test(HttpServletResponse response) throws IOException {

        ShowMessage showMessage = null;
        showMessage = ShowMessage.builder()
                .status(1)
                .msg("案例1")
                .build();
        response.setStatus(500);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");

         new ObjectMapper()
                .writeValue(
                        response.getWriter(),
                        showMessage
                );
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ShowMessage {
    private Integer status;
    private String msg;
}
