package com.travis.monolith.system.message.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessageTemplateParamType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Data;
import tools.jackson.core.type.TypeReference;

/** 新增消息模板请求参数。 */
@Data
public class SysMessageTemplateCreateReq {
    /** 合法模板参数键格式。 */
    private static final Pattern PARAM_KEY_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    /** 从模板正文中提取双花括号参数的表达式。 */
    private static final Pattern TEMPLATE_PARAM_PATTERN =
            Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");

    /** 模板编码。 */
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 64, message = "模板编码长度不能超过64个字符")
    private String templateCode;

    /** 模板名称。 */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String templateName;

    /** 推送通道。 */
    @NotBlank(message = "推送通道不能为空")
    @EnumValue(value = SysMessageChannel.class, message = "推送通道错误")
    @Size(max = 32, message = "推送通道长度不能超过32个字符")
    private String channel;

    /** 模板标题。 */
    @Size(max = 255, message = "模板标题长度不能超过255个字符")
    private String title;

    /** 外部平台模板 ID。 */
    @Size(max = 128, message = "平台模板ID长度不能超过128个字符")
    private String platformTemplateId;

    /** 模板参数结构，使用 JSON 对象格式。 */
    @Size(max = 4000, message = "字段结构长度不能超过4000个字符")
    @JsonValue(message = "字段结构必须是合法JSON对象")
    private String contentSchema;

    /** 模板 HTML 内容。 */
    @SanitizeHtml
    @Size(max = 5000, message = "模板内容长度不能超过5000个字符")
    @NotBlank(message = "模板内容不能为空")
    private String content;

    /** 点击消息后的跳转地址模板。 */
    @Size(max = 500, message = "跳转地址长度不能超过500个字符")
    private String redirectUrl;

    /** 模板状态。 */
    @NotNull(message = "状态不能为空")
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;

    /** 备注。 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    /** 校验要求标题的通道是否填写模板标题。 */
    @AssertTrue(message = "当前通道模板标题不能为空")
    public boolean isTitleValid() {
        return !SysMessageChannel.IN_APP.getValue().equals(channel)
                        && !SysMessageChannel.WECHAT_MP.getValue().equals(channel)
                        && !SysMessageChannel.WECHAT_OA.getValue().equals(channel)
                || StrUtil.isNotBlank(title);
    }

    /** 校验外部通道是否填写平台模板 ID。 */
    @AssertTrue(message = "外部通道平台模板ID不能为空")
    public boolean isPlatformTemplateIdValid() {
        return !SysMessageChannel.isExternal(channel) || StrUtil.isNotBlank(platformTemplateId);
    }

    /** 校验参数结构中的参数名、类型及描述配置。 */
    @AssertTrue(message = "字段参数配置错误")
    public boolean isContentSchemaConfigValid() {
        var schema = parseContentSchema();
        return schema == null || schema.entrySet().stream().allMatch(this::isParamConfigValid);
    }

    /** 校验正文引用的参数是否与参数结构完全一致。 */
    @AssertTrue(message = "模板参数引用与字段结构不一致")
    public boolean isTemplateParamUsageValid() {
        var schema = parseContentSchema();
        if (schema == null) {
            return true;
        }
        var contentKeys = extractTemplateParamKeys();
        return contentKeys != null && contentKeys.equals(schema.keySet());
    }

    /** 解析模板参数结构；格式错误时返回 {@code null}。 */
    private Map<String, SysMessageTemplateParamConfigReq> parseContentSchema() {
        if (StrUtil.isBlank(contentSchema)) {
            return Map.of();
        }
        try {
            return JsonUtil.parseObject(
                    contentSchema,
                    new TypeReference<
                            LinkedHashMap<String, SysMessageTemplateParamConfigReq>>() {});
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** 校验单个模板参数配置。 */
    private boolean isParamConfigValid(Map.Entry<String, SysMessageTemplateParamConfigReq> entry) {
        var key = entry.getKey();
        var config = entry.getValue();
        return StrUtil.isNotBlank(key)
                && PARAM_KEY_PATTERN.matcher(key).matches()
                && config != null
                && StrUtil.isNotBlank(config.getType())
                && SysMessageTemplateParamType.contains(config.getType())
                && config.getRequired() != null
                && isLengthValid(config.getLabel(), 100)
                && isLengthValid(config.getDescription(), 255);
    }

    /** 按正文出现顺序提取模板参数键。 */
    private LinkedHashSet<String> extractTemplateParamKeys() {
        var keys = new LinkedHashSet<String>();
        var matcher = TEMPLATE_PARAM_PATTERN.matcher(content);
        while (matcher.find()) {
            var key = matcher.group(1).trim();
            if (!PARAM_KEY_PATTERN.matcher(key).matches()) {
                return null;
            }
            keys.add(key);
        }
        return keys;
    }

    private boolean isLengthValid(String value, int max) {
        return value == null || value.length() <= max;
    }
}
