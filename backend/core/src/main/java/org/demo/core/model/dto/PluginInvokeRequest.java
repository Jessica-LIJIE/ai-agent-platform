package org.demo.core.model.dto;

import java.util.Map;

/**
 * 插件操作调用请求DTO
 */
public class PluginInvokeRequest {

    /**
     * 调用参数
     */
    private Map<String, Object> params;

    /**
     * 超时时间（毫秒），默认30秒
     */
    private Integer timeout;

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
