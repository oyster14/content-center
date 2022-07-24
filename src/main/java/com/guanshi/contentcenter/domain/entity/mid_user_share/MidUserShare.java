package com.guanshi.contentcenter.domain.entity.mid_user_share;

import javax.persistence.*;

import lombok.*;

/**
 * 表名：mid_user_share
 * 表注释：用户-分享中间表【描述用户购买的分享】
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "mid_user_share")
public class MidUserShare {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * share.id
     */
    @Column(name = "share_id")
    private Integer shareId;

    /**
     * user.id
     */
    @Column(name = "user_id")
    private Integer userId;
}