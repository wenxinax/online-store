package com.example.onlinestore.mapper;

import com.example.onlinestore.entity.ItemAccessLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品访问日志Mapper
 */
@Mapper
public interface ItemAccessLogMapper {

    /**
     * 插入单条商品访问日志记录
     *
     * @param log 商品访问日志实体对象，包含访问记录详细信息
     */
    void insertAccessLog(ItemAccessLogEntity log);

    /**
     * 批量插入商品访问日志记录
     *
     * @param accessLogEntities 商品访问日志实体对象集合，用于批量插入操作
     */
    void batchInsertAccessLogs(List<ItemAccessLogEntity> accessLogEntities);

    /**
     * 统计指定商品的累计访问次数
     *
     * @param itemId 商品唯一标识符
     * @return 该商品的累计访问总次数
     */
    int countByItemId(@Param("itemId") Long itemId);

    /**
     * 统计指定商品在时间范围内的访问次数
     *
     * @param itemId    商品唯一标识符
     * @param startTime 统计开始时间（包含）
     * @param endTime   统计结束时间（不包含）
     * @return 该商品在指定时间区间内的访问次数
     */
    int countByItemIdAndTimeRange(
        @Param("itemId") Long itemId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询热门商品排行榜
     *
     * @param startTime 统计开始时间（包含）
     * @param endTime   统计结束时间（不包含）
     * @param limit     返回结果数量限制
     * @return 包含商品ID和访问次数的Map集合，按访问次数降序排列
     *         Map结构说明：
     *         - key:"itemId" 对应商品ID
     *         - key:"count" 对应访问次数
     */
    List<Map<String, Object>> findHotItems(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("limit") int limit
    );

} 