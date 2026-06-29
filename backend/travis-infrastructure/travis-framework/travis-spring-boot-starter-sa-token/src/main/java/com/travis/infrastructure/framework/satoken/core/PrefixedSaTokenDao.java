package com.travis.infrastructure.framework.satoken.core;

import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import java.util.List;
import org.springframework.util.StringUtils;

/** 自动套用项目 Redis 前缀的 Sa-Token Redis DAO。 */
public class PrefixedSaTokenDao extends SaTokenDaoForRedisTemplate {

    private final String keyPrefix;

    public PrefixedSaTokenDao(String keyPrefix) {
        this.keyPrefix = normalize(keyPrefix);
    }

    @Override
    public String get(String key) {
        return super.get(resolve(key));
    }

    @Override
    public void set(String key, String value, long timeout) {
        super.set(resolve(key), value, timeout);
    }

    @Override
    public void update(String key, String value) {
        super.update(resolve(key), value);
    }

    @Override
    public void delete(String key) {
        super.delete(resolve(key));
    }

    @Override
    public long getTimeout(String key) {
        return super.getTimeout(resolve(key));
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        super.updateTimeout(resolve(key), timeout);
    }

    @Override
    public List<String> searchData(
            String prefix, String keyword, int start, int size, boolean sortType) {
        return super.searchData(resolve(prefix), keyword, start, size, sortType).stream()
                .map(this::removePrefix)
                .toList();
    }

    private String resolve(String key) {
        if (!StringUtils.hasText(keyPrefix) || !StringUtils.hasText(key)) {
            return key;
        }
        if (key.startsWith(keyPrefix)) {
            return key;
        }
        return keyPrefix + key;
    }

    private String removePrefix(String key) {
        if (!StringUtils.hasText(keyPrefix) || key == null || !key.startsWith(keyPrefix)) {
            return key;
        }
        return key.substring(keyPrefix.length());
    }

    private String normalize(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
