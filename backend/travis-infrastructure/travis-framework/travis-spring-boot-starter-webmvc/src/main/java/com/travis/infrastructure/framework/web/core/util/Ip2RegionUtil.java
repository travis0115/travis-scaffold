package com.travis.infrastructure.framework.web.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import java.net.InetAddress;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

/**
 * 基于ip2region 3.x的离线IP地址解析工具 支持国内外IPv4和IPv6地址解析到省/市级别 使用Ip2Region统一查询服务，自动识别IPv4/IPv6
 *
 * @author travis
 */
@Slf4j
@UtilityClass
public class Ip2RegionUtil {

    private static DatabaseReader databaseReader;

    private static final String[] LOCALES = {"zh-CN", "en", "es", "de", "fr", "pt-BR", "ja", "ru"};
    private static final String INTERNAL = "内网";
    private static final String UNKNOWN = "未知";

    static {
        try {
            // 读取 resources 下的 mmdb 文件
            var inputStream = new ClassPathResource("geolite2/GeoLite2-City.mmdb").getInputStream();
            // 构建 DatabaseReader 实例
            databaseReader = new DatabaseReader.Builder(inputStream).build();
            log.info("geoLite2 离线IP数据库加载成功");
        } catch (Exception e) {
            log.error("geoLite2 离线IP数据库加载失败，IP地址解析功能将不可用", e);
        }
    }

    /** 获取IP地址信息 */
    private static CityResponse getResponse(String ip) throws Exception {
        if (databaseReader == null) {
            return null;
        }
        var ipAddress = InetAddress.getByName(ip);
        return databaseReader.city(ipAddress);
    }

    /** 根据IP地址获取国家信息 */
    public static String getCountryByIP(String ip) throws Exception {
        if (IpUtil.isInternalIp(ip)) {
            return INTERNAL;
        }
        // 获取查询结果
        CityResponse response = getResponse(ip);
        if (response == null) {
            return UNKNOWN;
        }

        // 获取国家信息
        var country = getName(response.country().names());
        return StrUtil.blankToDefault(country, UNKNOWN);
    }

    /** 根据IP地址获取地址信息 格式：国家 省份 城市 */
    public static String getRegionByIP(String ip) {
        if (IpUtil.isInternalIp(ip)) {
            return INTERNAL;
        }
        // 获取查询结果
        CityResponse response;
        try {
            response = getResponse(ip);
        } catch (Exception e) {
            log.error("geoLite2 解析IP地址失败", e);
            return UNKNOWN;
        }
        if (response == null) {
            return UNKNOWN;
        }

        var result =
                CollUtil.newArrayList(
                                getName(response.country().names()),
                                getName(response.mostSpecificSubdivision().names()),
                                getName(response.city().names()))
                        .stream()
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.joining(" "));

        return StrUtil.blankToDefault(result, UNKNOWN);
    }

    /**
     * 从names中获取第一个非空的值
     *
     * @param names
     * @return
     */
    private static String getName(Map<String, String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }

        for (String locale : LOCALES) {
            String value = names.get(locale);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }

        return "";
    }
}
