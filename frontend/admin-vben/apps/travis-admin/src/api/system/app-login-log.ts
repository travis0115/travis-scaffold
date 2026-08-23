import { requestClient } from '#/api/request';

export namespace AppUserLoginLogApi {
  export interface Dashboard {
    todayLoginUsers: number;
  }
}

async function getAppUserLoginDashboard() {
  return requestClient.get<AppUserLoginLogApi.Dashboard>(
    '/app/user-login-log/dashboard',
  );
}

export { getAppUserLoginDashboard };
