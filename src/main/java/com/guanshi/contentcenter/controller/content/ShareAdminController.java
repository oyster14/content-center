package com.guanshi.contentcenter.controller.content;

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
    public Share auditById(@PathVariable Integer id, @RequestBody ShareAuditDTO auditDTO) {
        //TODO 认证、授权
        return this.shareService.auditById(id, auditDTO);


    }
}
