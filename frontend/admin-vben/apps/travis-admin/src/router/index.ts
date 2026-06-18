import { createRouterGuard } from './guard';
import { resetRoutes, router } from './instance';

// 创建路由守卫
createRouterGuard(router);

export { resetRoutes, router };
