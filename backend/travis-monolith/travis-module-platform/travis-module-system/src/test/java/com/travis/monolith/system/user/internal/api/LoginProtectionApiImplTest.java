package com.travis.monolith.system.user.internal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.config.api.SysConfigApi;
import com.travis.monolith.system.user.internal.service.LoginProtectionStore;
import org.junit.jupiter.api.Test;

class LoginProtectionApiImplTest {

    @Test
    void shouldLockAdminAccountAtDefaultThreshold() {
        var configApi = defaultConfigApi();
        var store = mock(LoginProtectionStore.class);
        when(store.increment(
                        argThat(key -> key != null && key.contains(":failure:account:")),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(5L);
        when(store.increment(
                        argThat(key -> key != null && key.contains(":failure:ip:")),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(1L);
        var service = new LoginProtectionApiImpl(configApi, store);

        var exception =
                catchThrowableOfType(
                        BizException.class,
                        () -> service.recordFailure(LoginType.ADMIN, "Admin", "127.0.0.1"));

        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.TOO_MANY_REQUESTS);
        verify(store)
                .lock(
                        argThat(key -> key != null && key.contains(":lock:account:")),
                        org.mockito.ArgumentMatchers.eq(900_000L));
    }

    @Test
    void shouldApplyDynamicAccountThreshold() {
        var configApi = defaultConfigApi();
        when(configApi.getValue("security.login.admin.account-max-failures")).thenReturn("10");
        var store = mock(LoginProtectionStore.class);
        when(store.increment(
                        argThat(key -> key != null && key.contains(":failure:account:")),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(5L);
        when(store.increment(
                        argThat(key -> key != null && key.contains(":failure:ip:")),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(1L);
        var service = new LoginProtectionApiImpl(configApi, store);

        service.recordFailure(LoginType.ADMIN, "Admin", "127.0.0.1");

        verify(store, never()).lock(anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldFailOpenWhenRedisIsUnavailable() {
        var store = mock(LoginProtectionStore.class);
        when(store.isLocked(anyString())).thenThrow(new IllegalStateException("redis down"));
        var service = new LoginProtectionApiImpl(defaultConfigApi(), store);

        service.checkAllowed(LoginType.ADMIN, "Admin", "127.0.0.1");
    }

    @Test
    void shouldClearOnlyAccountFailureAfterSuccess() {
        var store = mock(LoginProtectionStore.class);
        var service = new LoginProtectionApiImpl(defaultConfigApi(), store);

        service.recordSuccess(LoginType.APP, "User");

        verify(store).delete(argThat(key -> key != null && key.contains(":failure:account:")));
    }

    private SysConfigApi defaultConfigApi() {
        var configApi = mock(SysConfigApi.class);
        when(configApi.getValue(anyString())).thenReturn("");
        return configApi;
    }
}
