package com.example.onlinestore.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品访问日志实体
 */
@Setter
@Getter
public class ItemAccessLogEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2096934412191760686L;
    /**
     * 主键ID，唯一标识记录
     */
    private Long id;

    /**
     * 关联项ID，关联其他业务实体（如商品、文章等）
     */
    private Long itemId;

    /**
     * 会员标识，未登录用户则为空
     */
    private String memberId;

    /**
     * 客户端IP地址，记录访问来源
     */
    private String ip;

    /**
     * 客户端浏览器/设备标识，用于用户终端识别
     */
    private String userAgent;

    /**
     * 来源页面URL，记录跳转前的页面地址
     */
    private String referer;

    /**
     * 访问发生的时间戳，精确到毫秒
     */
    private LocalDateTime accessTime;

    /**
     * 访问次数计数器，用于累计统计
     */
    private Integer accessCount;
    /**
     * 记录创建时间，由系统自动维护
     */
    private LocalDateTime createdAt;
}