package com.guanshi.contentcenter.controller.content;

import com.guanshi.contentcenter.auth.CheckLogin;
import com.guanshi.contentcenter.domain.dto.content.ShareDTO;
import com.guanshi.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))

public class ShareController {
    private final ShareService shareService;

    @GetMapping("/{id}")
    @CheckLogin
    public ShareDTO findById(
            @PathVariable Integer id
    ) {
        return this.shareService.findById(id);
    }

}
