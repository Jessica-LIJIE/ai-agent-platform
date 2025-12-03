package org.demo.core.model.dto;

/**
 * 插件状态更新请求DTO
 */
public class PluginStatusUpdateRequest {

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
