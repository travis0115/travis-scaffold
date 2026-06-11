/**
 * 该文件可自行根据业务逻辑进行调整
 */
import type { RequestClientOptions } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  defaultResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { useAccessStore, useRequestLoadingStore } from '@vben/stores';
import { startProgress, stopProgress } from '@vben/utils';

import { message, Modal } from 'antdv-next';

import { $t } from '#/locales';
import { useAuthStore } from '#/store';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

/**
 * 根据请求 config 中记录的 loading 类型，关闭对应的 loading
 */
function handleLoadingDone(config?: any) {
  const loadingType = config?._loadingType;
  if (loadingType === false) return;
  if (loadingType === 'nprogress') {
    stopProgress();
  } else {
    useRequestLoadingStore().dec();
  }
}

function createRequestClient(baseURL: string, options?: RequestClientOptions) {
  const client = new RequestClient({
    ...options,
    baseURL,
  });

  /**
   * 重新认证逻辑（带防重入锁，避免多个并发 401 重复触发登出）
   */
  let isReAuthenticating = false;
  let isErrorModalOpen = false;

  async function doReAuthenticate() {
    if (isReAuthenticating) return;
    isReAuthenticating = true;
    try {
      console.warn('Access token or refresh token is invalid or expired. ');
      const accessStore = useAccessStore();
      const authStore = useAuthStore();
      accessStore.setAccessToken(null);
      if (
        preferences.app.loginExpiredMode === 'modal' &&
        accessStore.isAccessChecked
      ) {
        accessStore.setLoginExpired(true);
      } else {
        // 提示用户登录已失效，点击确定后跳转登录页
        Modal.warning({
          title: '登录失效',
          content: '登录信息已失效，请重新登录',
          okText: $t('common.gotIt'),
          async onOk() {
            await authStore.logout();
          },
        });
      }
    } finally {
      isReAuthenticating = false;
    }
  }

  /**
   * 刷新token逻辑
   */
  async function doRefreshToken() {
    const accessStore = useAccessStore();
    const resp = await refreshTokenApi();
    const newToken = resp.data;
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `Bearer ${token}` : null;
  }

  // 请求头处理 + 全局 Loading
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      config.headers['Client-Type'] = 'web';

      // 处理 loading：默认 fullscreen，可单独指定 'nprogress' 或 false
      const loading = (config as any).loading;
      if (loading !== false) {
        if (loading === 'nprogress') {
          startProgress();
        } else {
          // 默认 fullscreen loading（与页面切换 loading 一致，受主题配置控制）
          useRequestLoadingStore().inc();
        }
        (config as any)._loadingType = loading;
      }

      return config;
    },
  });

  // 响应成功时关闭 loading
  client.addResponseInterceptor({
    fulfilled: (response) => {
      handleLoadingDone(response.config);
      return response;
    },
  });

  // 处理返回的响应数据格式
  client.addResponseInterceptor(
    defaultResponseInterceptor({
      codeField: 'code',
      dataField: 'data',
      successCode: '200',
    }),
  );

  // token过期的处理（同时兼容 HTTP 401 和响应体 code "401"）
  const authInterceptor = authenticateResponseInterceptor({
    client,
    doReAuthenticate,
    doRefreshToken,
    enableRefreshToken: preferences.app.enableRefreshToken,
    formatToken,
  });
  client.addResponseInterceptor({
    ...authInterceptor,
    rejected: async (error: any) => {
      handleLoadingDone(error?.config);

      // 后端返回 HTTP 200 但响应体 code 为 "401" 时，也视为认证失败
      const responseData = error?.response?.data ?? error?.data;
      if (
        error?.response?.status !== 401 &&
        responseData?.code === '401'
      ) {
        await doReAuthenticate();
        throw error;
      }

      return authInterceptor.rejected?.(error);
    },
  });

  // 通用的错误处理,如果没有进入上面的错误处理逻辑，就会进入这里
  client.addResponseInterceptor(
    errorMessageResponseInterceptor((msg: string, error) => {
      handleLoadingDone(error?.config);

      const responseData = error?.response?.data ?? error?.data ?? {};

      // 业务层 401 已由上方 auth 拦截器处理（清除 token + 跳转登录页），此处不再重复弹提示
      if (responseData?.code === '401') {
        return;
      }

      // 优先展示服务端返回的错误信息，而不是本地生成的通用错误提示
      const serverMessage =
        responseData?.msg ?? responseData?.error ?? responseData?.message ?? '';
      const finalMessage = serverMessage || msg;

      const errorType = error?.config?.errorMessageType;

      if (errorType === false) {
        // 不弹窗，由调用方自行处理
        return;
      }

      if (errorType === 'message') {
        message.error(finalMessage);
        return;
      }

      // 默认使用 Modal 弹窗
      if (isErrorModalOpen) {
        return;
      }
      isErrorModalOpen = true;
      Modal.error({
        title: '操作失败',
        content: finalMessage,
        okText: $t('common.gotIt'),
        afterClose() {
          isErrorModalOpen = false;
        },
      });
    }),
  );

  return client;
}

export const requestClient = createRequestClient(apiURL, {
  responseReturn: 'data',
});

export const baseRequestClient = new RequestClient({ baseURL: apiURL });
