package org.demo.core.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 * 用于自动填充创建时间、更新时间等字段
 */
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "create_time", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "created_at", LocalDateTime.class, LocalDateTime.now());

        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "update_time", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updated_at", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "update_time", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updated_at", LocalDateTime.class, LocalDateTime.now());
    }
}
