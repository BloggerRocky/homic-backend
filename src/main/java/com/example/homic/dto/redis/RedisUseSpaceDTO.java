package com.example.homic.dto.redis;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/13.17:06
 * 项目名：homic
 */
//用于存放用户空间信息的Redis包装实体类
public class RedisUseSpaceDTO {
    private Long totalSpace;
    private Long useSpace;

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public Long getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(Long useSpace) {
        this.useSpace = useSpace;
    }
}
