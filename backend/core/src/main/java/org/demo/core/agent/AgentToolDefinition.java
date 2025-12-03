package org.demo.core.agent;

import java.util.List;
import java.util.Map;

/**
 * Agent工具定义
 * 用于LLM Function Calling，定义可供智能体调用的工具
 * 兼容OpenAI Function Calling格式
 */
public class AgentToolDefinition {

    /**
     * 工具类型（固定为"function"，兼容OpenAI格式）
     */
    private String type = "function";

    /**
     * 函数定义
     */
    private FunctionDefinition function;

    /**
     * 工具来源：plugin（插件）、builtin（内置）、custom（自定义）
     */
    private String source;

    /**
     * 关联的插件ID（当source为plugin时）
     */
    private String pluginId;

    /**
     * 关联的操作ID（当source为plugin时）
     */
    private String operationId;

    /**
     * 函数定义（兼容OpenAI格式）
     */
    public static class FunctionDefinition {
        /**
         * 函数名称（唯一标识符）
         */
        private String name;

        /**
         * 函数描述（供LLM理解用途）
         */
        private String description;

        /**
         * 参数定义（JSON Schema格式）
         */
        private ParametersSchema parameters;

        /**
         * 是否为严格模式（OpenAI structured outputs）
         */
        private Boolean strict;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ParametersSchema getParameters() {
            return parameters;
        }

        public void setParameters(ParametersSchema parameters) {
            this.parameters = parameters;
        }

        public Boolean getStrict() {
            return strict;
        }

        public void setStrict(Boolean strict) {
            this.strict = strict;
        }
    }

    /**
     * 参数Schema定义（JSON Schema格式）
     */
    public static class ParametersSchema {
        /**
         * 类型（固定为"object"）
         */
        private String type = "object";

        /**
         * 属性定义
         */
        private Map<String, PropertyDefinition> properties;

        /**
         * 必填字段列表
         */
        private List<String> required;

        /**
         * 是否允许额外属性
         */
        private Boolean additionalProperties;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, PropertyDefinition> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, PropertyDefinition> properties) {
            this.properties = properties;
        }

        public List<String> getRequired() {
            return required;
        }

        public void setRequired(List<String> required) {
            this.required = required;
        }

        public Boolean getAdditionalProperties() {
            return additionalProperties;
        }

        public void setAdditionalProperties(Boolean additionalProperties) {
            this.additionalProperties = additionalProperties;
        }
    }

    /**
     * 属性定义
     */
    public static class PropertyDefinition {
        /**
         * 属性类型：string, number, integer, boolean, array, object
         */
        private String type;

        /**
         * 属性描述
         */
        private String description;

        /**
         * 枚举值（可选）
         */
        private List<String> enumValues;

        /**
         * 默认值（可选）
         */
        private Object defaultValue;

        /**
         * 数组元素类型（当type为array时）
         */
        private PropertyDefinition items;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getEnumValues() {
            return enumValues;
        }

        public void setEnumValues(List<String> enumValues) {
            this.enumValues = enumValues;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public PropertyDefinition getItems() {
            return items;
        }

        public void setItems(PropertyDefinition items) {
            this.items = items;
        }
    }

    /**
     * 从插件操作创建工具定义
     */
    public static AgentToolDefinition fromPluginOperation(
            String pluginId,
            String operationId,
            String name,
            String description,
            Map<String, Object> inputSchema) {

        AgentToolDefinition tool = new AgentToolDefinition();
        tool.setType("function");
        tool.setSource("plugin");
        tool.setPluginId(pluginId);
        tool.setOperationId(operationId);

        FunctionDefinition function = new FunctionDefinition();
        function.setName(name);
        function.setDescription(description);

        // 将inputSchema转换为ParametersSchema
        if (inputSchema != null) {
            ParametersSchema params = convertToParametersSchema(inputSchema);
            function.setParameters(params);
        }

        tool.setFunction(function);
        return tool;
    }

    /**
     * 将插件的inputSchema转换为ParametersSchema
     */
    @SuppressWarnings("unchecked")
    private static ParametersSchema convertToParametersSchema(Map<String, Object> inputSchema) {
        ParametersSchema params = new ParametersSchema();
        params.setType("object");

        Object properties = inputSchema.get("properties");
        if (properties instanceof Map) {
            Map<String, Object> propsMap = (Map<String, Object>) properties;
            Map<String, PropertyDefinition> propDefs = new java.util.HashMap<>();

            for (Map.Entry<String, Object> entry : propsMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    PropertyDefinition propDef = convertToPropertyDefinition((Map<String, Object>) entry.getValue());
                    propDefs.put(entry.getKey(), propDef);
                }
            }
            params.setProperties(propDefs);
        }

        Object required = inputSchema.get("required");
        if (required instanceof List) {
            params.setRequired((List<String>) required);
        }

        return params;
    }

    /**
     * 转换属性定义
     */
    @SuppressWarnings("unchecked")
    private static PropertyDefinition convertToPropertyDefinition(Map<String, Object> propMap) {
        PropertyDefinition propDef = new PropertyDefinition();
        propDef.setType((String) propMap.get("type"));
        propDef.setDescription((String) propMap.get("description"));

        Object enumVal = propMap.get("enum");
        if (enumVal instanceof List) {
            propDef.setEnumValues((List<String>) enumVal);
        }

        propDef.setDefaultValue(propMap.get("default"));

        Object items = propMap.get("items");
        if (items instanceof Map) {
            propDef.setItems(convertToPropertyDefinition((Map<String, Object>) items));
        }

        return propDef;
    }

    // =============== Getters and Setters ===============

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FunctionDefinition getFunction() {
        return function;
    }

    public void setFunction(FunctionDefinition function) {
        this.function = function;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
