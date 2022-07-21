package com.guanshi.contentcenter.controller.content;

import com.guanshi.contentcenter.auth.CheckAuthorization;
import com.guanshi.contentcenter.domain.dto.content.ShareAuditDTO;
import com.guanshi.contentcenter.domain.entity.content.Share;
import com.guanshi.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareAdminController {
    private final ShareService shareService;
    @PutMapping("/audit/{id}")
    @CheckAuthorization("admin")
    public Share auditById(@PathVariable Integer id, @RequestBody ShareAuditDTO auditDTO) {

        return this.shareService.auditById(id, auditDTO);
    }
}
