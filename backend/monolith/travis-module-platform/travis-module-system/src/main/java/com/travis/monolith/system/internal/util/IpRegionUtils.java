package com.travis.monolith.system.internal.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 基于ip2region 3.x的离线IP地址解析工具
 * 支持国内外IPv4和IPv6地址解析到省/市级别
 * 使用Ip2Region统一查询服务，自动识别IPv4/IPv6
 *
 * @author travis
 */
@Slf4j
@Component
public class IpRegionUtils {

    private Ip2Region ip2Region;

    /**
     * 初始化ip2region查询服务，从classpath加载v4和v6的xdb文件到内存
     */
    @PostConstruct
    public void init() {
        try {
            // 加载IPv4 xdb到内存
            byte[] v4Buffer = loadXdb("ip2region/ip2region_v4.xdb");
            Config v4Config = Config.custom()
                    .setCachePolicy(Config.BufferCache)
                    .setXdbInputStream(new ByteArrayInputStream(v4Buffer))
                    .asV4();

            // 加载IPv6 xdb到内存
            byte[] v6Buffer = loadXdb("ip2region/ip2region_v6.xdb");
            Config v6Config = Config.custom()
                    .setCachePolicy(Config.BufferCache)
                    .setXdbInputStream(new ByteArrayInputStream(v6Buffer))
                    .asV6();

            ip2Region = Ip2Region.create(v4Config, v6Config);
            log.info("ip2region离线IP数据库加载成功（IPv4 + IPv6双栈）");
        } catch (Exception e) {
            log.error("ip2region离线IP数据库加载失败，IP地址解析功能将不可用", e);
        }
    }

    /**
     * 解析IP地址到地理位置
     * 内网IP返回 "内网"
     * 自动识别IPv4和IPv6地址
     *
     * @param ip IP地址（支持IPv4和IPv6）
     * @return 地理位置描述，如 "中国 广东省深圳市" 或 "美国 California"
     */
    public String getRegion(String ip) {
        if (ip == null || ip.isEmpty() || IpUtils.isInternalIp(ip)) {
            return "内网";
        }

        if (ip2Region == null) {
            return "未知";
        }

        try {
            String region = ip2Region.search(ip);
            if (region == null || region.isEmpty()) {
                return "未知";
            }
            return formatRegion(region);
        } catch (Exception e) {
            log.debug("IP地址解析失败: {}", ip);
            return "未知";
        }
    }

    /**
     * 格式化ip2region的原始输出
     * 3.x格式："国家|省份|城市|ISP|iso-alpha2-code"
     * 转换为友好的格式，如 "中国 广东省深圳市"
     */
    private String formatRegion(String rawRegion) {
        if (rawRegion == null || rawRegion.isEmpty()) {
            return "未知";
        }

        String[] parts = rawRegion.split("\\|");
        String country = getPart(parts, 0);
        String province = getPart(parts, 1);
        String city = getPart(parts, 2);

        StringBuilder sb = new StringBuilder();

        if (!country.isEmpty()) {
            sb.append(country);
        }

        if (!province.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(province);
        }

        if (!city.isEmpty() && !city.equals(province)) {
            if (!sb.isEmpty()) {
                // 国内省份带"省"/"市"/"自治区"后缀时，城市直接拼接
                if (province.endsWith("省") || province.endsWith("市")
                        || province.endsWith("自治区") || province.endsWith("特别行政区")) {
                    sb.append(city);
                } else {
                    sb.append(" ").append(city);
                }
            } else {
                sb.append(city);
            }
        }

        return sb.isEmpty() ? "未知" : sb.toString();
    }

    private String getPart(String[] parts, int index) {
        if (index < parts.length && parts[index] != null && !"0".equals(parts[index])) {
            return parts[index];
        }
        return "";
    }

    private byte[] loadXdb(String classpath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpath);
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToByteArray(is);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ip2Region != null) {
            try {
                ip2Region.close();
            } catch (Exception e) {
                log.debug("ip2region关闭时异常", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
