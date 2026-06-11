package com.travis.infrastructure.framework.xxljob.core;

import com.travis.infrastructure.framework.xxljob.config.properties.TravisXxlJobProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** 生成供独立 XXL-JOB Admin 校验的一次性短时登录地址。 */
public class XxlJobSsoUrlService {

    private final TravisXxlJobProperties properties;

    public XxlJobSsoUrlService(TravisXxlJobProperties properties) {
        this.properties = properties;
    }

    public String createLoginUrl(String userId) {
        var sso = properties.sso();
        if (!sso.enabled() || sso.secret().isBlank()) {
            return properties.admin().webUrl();
        }
        long expiresAt = Instant.now().plusSeconds(sso.ttlSeconds()).getEpochSecond();
        String payload = userId + ":" + expiresAt;
        String signature = sign(payload, sso.secret());
        return properties.admin().webUrl()
                + "/auth/sso?userId="
                + encode(userId)
                + "&expiresAt="
                + expiresAt
                + "&signature="
                + signature;
    }

    private static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.HexFormat.of()
                    .formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("生成 XXL-JOB SSO 签名失败", ex);
        }
    }

    public static boolean verify(String payload, String signature, String secret) {
        String expected = sign(payload, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
