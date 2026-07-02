import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { message, notification } from 'antdv-next';
import { defineStore } from 'pinia';

import { getAccessCodesApi, getUserInfoApi, loginApi, logoutApi } from '#/api';
import { $t } from '#/locales';
import { router } from '#/router/instance';

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();

  const loginLoading = ref(false);

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param params 登录表单数据
   */
  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    // 异步处理用户登录操作并从响应头获取 accessToken
    let userInfo: null | UserInfo = null;
    try {
      loginLoading.value = true;
      const accessToken = await loginApi(params);

      // 如果成功获取到 accessToken
      if (accessToken) {
        accessStore.setAccessToken(accessToken);

        // 获取用户信息并存储到 accessStore 中
        const [fetchUserInfoResult] = await Promise.all([
          fetchUserInfo(),
          fetchAccessCodes(),
        ]);

        userInfo = fetchUserInfoResult;

        userStore.setUserInfo(userInfo);

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          onSuccess
            ? await onSuccess?.()
            : await router.push(
                userInfo.homePath || preferences.app.defaultHomePath,
              );
        }

        if (userInfo?.nickname) {
          notification.success({
            description: `${$t('authentication.loginSuccessDesc')}:${userInfo?.nickname}`,
            duration: 3,
            title: $t('authentication.loginSuccess'),
          });
        }
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '登录失败');
      // 登录失败，通知动画组件
      document.dispatchEvent(new CustomEvent('travis-login-error'));
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  const isLoggingOut = ref(false);

  async function logout(redirect: boolean = true) {
    if (isLoggingOut.value) return;
    isLoggingOut.value = true;

    try {
      if (typeof window !== 'undefined') {
        window.dispatchEvent(
          new CustomEvent('travis:close-notification-socket'),
        );
      }

      try {
        // 仅在 token 有效时调用后端登出接口，避免 token 已失效时触发 401 死循环
        if (accessStore.accessToken) {
          await logoutApi();
        }
      } catch {
        // 不做任何处理
      }

      resetAllStores();
      accessStore.setLoginExpired(false);

      const currentRoute = router.currentRoute.value;
      if (currentRoute.path === LOGIN_PATH) {
        return;
      }

      // 回登录页带上当前路由地址
      await router.replace({
        path: LOGIN_PATH,
        query: redirect
          ? {
              redirect: encodeURIComponent(currentRoute.fullPath),
            }
          : {},
      });
    } finally {
      isLoggingOut.value = false;
    }
  }

  async function fetchUserInfo() {
    const userInfo = await getUserInfoApi();
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  async function fetchAccessCodes() {
    const accessCodes = await getAccessCodesApi();
    accessStore.setAccessCodes(accessCodes);
    return accessCodes;
  }

  function $reset() {
    loginLoading.value = false;
    isLoggingOut.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchAccessCodes,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
