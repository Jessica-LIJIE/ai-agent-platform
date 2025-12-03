package org.demo.core.exception;

/**
 * 插件不存在异常
 */
public class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException(String pluginId) {
        super("插件不存在: " + pluginId);
    }

    public PluginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
