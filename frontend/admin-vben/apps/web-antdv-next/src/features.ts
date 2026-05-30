/**
 * 功能开关配置
 * 通过环境变量控制各模块的启用/禁用状态
 */

/**
 * 判断部门功能是否开启
 * 环境变量 VITE_DEPT_ENABLED 为 'true' 时开启
 */
export function isDeptEnabled(): boolean {
  return import.meta.env.VITE_DEPT_ENABLED === 'true';
}
