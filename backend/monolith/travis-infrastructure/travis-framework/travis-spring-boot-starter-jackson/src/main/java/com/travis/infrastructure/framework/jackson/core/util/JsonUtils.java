package com.travis.infrastructure.framework.jackson.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.travis.infrastructure.framework.jackson.core.model.JsonObject;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 工具类
 * ObjectMapper 由 Spring 注入后写入静态 holder，未注入时使用默认实例。
 * 
 * <p>线程安全设计：</p>
 * <ul>
 *     <li>{@link #objectMapper} 使用 volatile 修饰，保证多线程环境下的可见性，防止指令重排序导致读取到未完全初始化的对象</li>
 *     <li>所有静态方法都是无状态的，可以安全并发调用</li>
 * </ul>
 */
@Slf4j
public class JsonUtils {

    /**
     * 默认 ObjectMapper 实例，用于 Spring 未注入时的兜底
     */
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    /**
     * Spring 注入的 ObjectMapper 实例
     * <p>使用 volatile 修饰的原因：</p>
     * <ol>
     *     <li><strong>禁止指令重排序</strong>：防止在构造函数初始化时，先赋值引用再完成对象初始化，导致其他线程读取到半初始化的对象</li>
     *     <li><strong>保证内存可见性</strong>：一个线程写入的 ObjectMapper 对其他线程立即可见</li>
     * </ol>
     * <p>典型场景：Spring 容器启动时通过构造函数注入 ObjectMapper，业务线程并发调用静态方法使用该实例</p>
     */
    private static volatile ObjectMapper objectMapper;

    /**
     * 构造方法，Spring 通过此方法注入 ObjectMapper
     * <p>注意：该方法是包访问权限，只能被同一包内的 Spring 配置类调用</p>
     *
     * @param objectMapper Spring 容器创建的 ObjectMapper 实例（已配置序列化器、反序列化器等）
     */
    JsonUtils(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    /**
     * 统一获取 ObjectMapper，优先使用 Spring 注入的实例
     * <p>如果 Spring 尚未注入（如单元测试环境），则返回默认的 ObjectMapper 实例</p>
     *
     * @return Spring 注入的 ObjectMapper 或默认实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : DEFAULT_MAPPER;
    }


    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param object 待序列化的对象
     * @return JSON 字符串，如果对象为 null 则返回 null
     * @throws RuntimeException 序列化失败时抛出运行时异常
     */
    public static String toJsonString(Object object) {
        if (object == null) return null;
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为 JSON 字节数组
     *
     * @param object 待序列化的对象
     * @return JSON 字节数组，如果对象为 null 则返回 null
     * @throws RuntimeException 序列化失败时抛出运行时异常
     */
    public static byte[] toJsonByte(Object object) {
        if (object == null) return null;
        try {
            return getObjectMapper().writeValueAsBytes(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为格式化的 JSON 字符串（带缩进）
     *
     * @param object 待序列化的对象
     * @return 格式化的 JSON 字符串，如果对象为 null 则返回 null
     * @throws RuntimeException 序列化失败时抛出运行时异常
     */
    public static String toJsonPrettyString(Object object) {
        if (object == null) return null;
        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     *
     * @param text  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象，如果字符串为空或解析失败则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readValue(text, clazz);
        } catch (Exception e) {
            log.error("json parse err, text length:{}", text.length(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串按指定路径提取后反序列化为指定类型的对象
     *
     * @param text  JSON 字符串
     * @param path  JSON 路径（如 "data.user"）
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象，如果字符串为空或路径不存在则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            JsonNode treeNode = getObjectMapper().readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return getObjectMapper().readValue(pathNode.toString(), clazz);
        } catch (Exception e) {
            log.error("json parse err, path:{}", path, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定 JavaType 的对象（支持复杂类型，如 List<Map>）
     *
     * @param text JSON 字符串
     * @param type JavaType 类型（通过 TypeReference 或 TypeFactory 构建）
     * @param <T>  泛型类型
     * @return 反序列化后的对象，如果字符串为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(String text, Type type) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readValue(text, getObjectMapper().getTypeFactory().constructType(type));
        } catch (Exception e) {
            log.error("json parse err, text length:{}", text.length(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字节数组反序列化为指定 JavaType 的对象
     *
     * @param text JSON 字节数组
     * @param type JavaType 类型
     * @param <T>  泛型类型
     * @return 反序列化后的对象，如果字节数组为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(byte[] text, Type type) {
        if (ArrayUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readValue(text, getObjectMapper().getTypeFactory().constructType(type));
        } catch (Exception e) {
            log.error("json parse err", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字节数组反序列化为指定类型的对象
     *
     * @param bytes JSON 字节数组
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象，如果字节数组为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) return null;
        try {
            return getObjectMapper().readValue(bytes, clazz);
        } catch (Exception e) {
            log.error("json parse err", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为 TypeReference 指定的复杂类型
     *
     * @param text          JSON 字符串
     * @param typeReference TypeReference 类型引用（如 new TypeReference<List<User>>() {}）
     * @param <T>           泛型类型
     * @return 反序列化后的对象，如果字符串为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readValue(text, typeReference);
        } catch (Exception e) {
            log.error("json parse err, text length:{}", text.length(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型，解析失败时返回 null（不抛异常）
     * <p>适用于容错场景，如可选的 JSON 字段解析</p>
     *
     * @param text          JSON 字符串
     * @param typeReference TypeReference 类型引用
     * @param <T>           泛型类型
     * @return 反序列化后的对象，如果解析失败或字符串为空则返回 null
     */
    public static <T> T parseObjectQuietly(String text, TypeReference<T> typeReference) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readValue(text, typeReference);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 宽松模式反序列化：当 Jackson 遇到 @JsonTypeInfo(use = Id.CLASS) 但 JSON 中没有 class 属性时会报错，此时用 Hutool 兜底
     * <p>适用于处理不完全符合规范的 JSON 数据</p>
     *
     * @param text  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象，如果字符串为空则返回 null
     */
    public static <T> T parseObjectLenient(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) return null;
        return JSONUtil.toBean(text, clazz);
    }

    /**
     * 将 JSON 字符串解析为 JsonObject（可变对象，支持链式操作）
     *
     * @param text JSON 字符串
     * @return JsonObject 对象
     */
    public static JsonObject parseObject(String text) {
        return JsonObject.of(text);
    }

    /**
     * 将 JSON 字符串反序列化为 List
     *
     * @param text  JSON 字符串
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return List 集合，如果字符串为空则返回空列表
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) return new ArrayList<>();
        try {
            return getObjectMapper().readValue(text,
                    getObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("json parse array err, text length:{}", text.length(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串按指定路径提取后反序列化为 List
     *
     * @param text  JSON 字符串
     * @param path  JSON 路径（如 "data.items"）
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return List 集合，如果字符串为空或路径不存在则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static <T> List<T> parseArray(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            JsonNode treeNode = getObjectMapper().readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return getObjectMapper().readValue(pathNode.toString(),
                    getObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("json parse array err, path:{}", path, e);
            throw new RuntimeException(e);
        }
    }

    // --------------- 树 / 原生节点 ---------------

    /**
     * 将 JSON 字符串解析为 JsonNode 树形结构
     *
     * @param text JSON 字符串
     * @return JsonNode 树节点，如果字符串为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static JsonNode parseTree(String text) {
        if (StrUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readTree(text);
        } catch (Exception e) {
            log.error("json parse tree err, text length:{}", text.length(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字节数组解析为 JsonNode 树形结构
     *
     * @param text JSON 字节数组
     * @return JsonNode 树节点，如果字节数组为空则返回 null
     * @throws RuntimeException 解析失败时抛出运行时异常并记录日志
     */
    public static JsonNode parseTree(byte[] text) {
        if (ArrayUtil.isEmpty(text)) return null;
        try {
            return getObjectMapper().readTree(text);
        } catch (Exception e) {
            log.error("json parse tree err", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断字符串是否是合法的 JSON 格式（包括对象和数组）
     *
     * @param text 待校验的字符串
     * @return true 如果是合法的 JSON 格式
     */
    public static boolean isJson(String text) {
        return JSONUtil.isTypeJSON(text);
    }

    /**
     * 判断字符串是否是合法的 JSON 对象格式（不包括数组）
     *
     * @param str 待校验的字符串
     * @return true 如果是合法的 JSON 对象格式
     */
    public static boolean isJsonObject(String str) {
        return JSONUtil.isTypeJSONObject(str);
    }



    /**
     * 将对象转换为目标类型（直接转换，避免先序列化再反序列化的性能损耗）
     * <p>如果对象已经是目标类型，则直接返回，不做转换</p>
     *
     * @param obj   源对象
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的对象
     */
    public static <T> T convertObject(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        return getObjectMapper().convertValue(obj, clazz);
    }

    /**
     * 将对象转换为 TypeReference 指定的复杂类型
     *
     * @param obj           源对象
     * @param typeReference TypeReference 类型引用
     * @param <T>           泛型类型
     * @return 转换后的对象
     */
    public static <T> T convertObject(Object obj, TypeReference<T> typeReference) {
        if (obj == null) return null;
        return getObjectMapper().convertValue(obj, typeReference);
    }

    /**
     * 将对象转换为 List 集合
     *
     * @param obj   源对象（可以是数组、Collection 等）
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return List 集合，如果源对象为 null 则返回空列表
     */
    public static <T> List<T> convertList(Object obj, Class<T> clazz) {
        if (obj == null) return new ArrayList<>();
        return getObjectMapper().convertValue(obj,
                getObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * 压缩为单行 JSON
     */
    public static String compactJson(String json) {
        try {
            var obj = parseObject(json, Object.class);
            return obj != null ? toJsonString(obj) : json;
        } catch (Exception e) {
            return json;
        }
    }
}