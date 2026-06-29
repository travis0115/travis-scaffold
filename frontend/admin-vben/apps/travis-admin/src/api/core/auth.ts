import type { RequestResponse } from '@vben/request';

import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
  }

  export interface RefreshTokenResult {
    data: string;
    status: number;
  }

  export interface WebSocketTicketResult {
    expiresIn: number;
    ticket: string;
  }
}

function normalizeToken(token: string) {
  return token.startsWith('Bearer ') ? token.slice(7) : token;
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  const response = await requestClient.post<RequestResponse>(
    '/system/auth/login',
    data,
    {
      responseReturn: 'raw',
    },
  );
  const getHeader = response.headers.get;
  const token =
    (typeof getHeader === 'function'
      ? getHeader.call(response.headers, 'Authorization')
      : undefined) ??
    response.headers.Authorization ??
    response.headers.authorization ??
    '';
  return normalizeToken(String(token));
}

/**
 * 刷新accessToken
 */
export async function refreshTokenApi() {
  return baseRequestClient.post<AuthApi.RefreshTokenResult>('/system/auth/refresh', {
    withCredentials: true,
  });
}

/**
 * 退出登录（使用 requestClient 确保 Token 随请求发送）
 */
export async function logoutApi() {
  return requestClient.post('/system/auth/logout');
}

/**
 * 签发 WebSocket 握手 ticket
 */
export async function createWebSocketTicketApi() {
  return requestClient.post<AuthApi.WebSocketTicketResult>('/system/auth/ws-ticket');
}

/**
 * 获取用户权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/system/auth/codes');
}
