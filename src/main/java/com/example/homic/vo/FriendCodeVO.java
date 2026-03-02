package com.example.homic.vo;

/**
 * 好友码信息VO
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
public class FriendCodeVO {
    /**
     * 好友码
     */
    private String code;

    /**
     * 剩余过期时间（秒）
     */
    private Long expiryTime;

    /**
     * 重新生成冷却时间（秒）
     */
    private Long regenerateCooldown;

    public FriendCodeVO() {
    }

    public FriendCodeVO(String code, Long expiryTime, Long regenerateCooldown) {
        this.code = code;
        this.expiryTime = expiryTime;
        this.regenerateCooldown = regenerateCooldown;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Long getRegenerateCooldown() {
        return regenerateCooldown;
    }

    public void setRegenerateCooldown(Long regenerateCooldown) {
        this.regenerateCooldown = regenerateCooldown;
    }
}
