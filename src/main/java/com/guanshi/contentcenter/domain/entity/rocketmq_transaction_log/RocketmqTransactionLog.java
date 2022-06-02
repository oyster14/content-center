package com.guanshi.contentcenter.domain.entity.rocketmq_transaction_log;

import javax.persistence.*;

import lombok.*;

/**
 * 表名：rocketmq_transaction_log
 * 表注释：RocketMQ事务日志表
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "rocketmq_transaction_log")
public class RocketmqTransactionLog {
    /**
     * id
     */
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 事务id
     */
    @Column(name = "transaction_Id")
    private String transactionId;

    /**
     * 日志
     */
    private String log;
}