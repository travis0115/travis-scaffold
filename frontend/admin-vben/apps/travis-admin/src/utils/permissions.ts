import { useAccess } from '@vben/access';

type OperationOption = Record<string, any> | string;

/**
 * 系统设置
 */
export const SYSTEM_PERMS = {
   // 用户管理
  userCreate: 'system:user:create',
  userDelete: 'system:user:delete',
  userUpdate: 'system:user:update',

  // 角色管理
  roleCreate: 'system:role:create',
  roleDelete: 'system:role:delete',
  roleUpdate: 'system:role:update',

  // 部门管理
  deptCreate: 'system:dept:create',
  deptDelete: 'system:dept:delete',
  deptUpdate: 'system:dept:update',

  // 菜单管理
  menuCreate: 'system:menu:create',
  menuDelete: 'system:menu:delete',
  menuUpdate: 'system:menu:update',

  // 字典管理
  dictCreate: 'system:dict:create',
  dictDelete: 'system:dict:delete',
  dictUpdate: 'system:dict:update',

  // 参数配置
  configCreate: 'system:config:create',
  configDelete: 'system:config:delete',
  configUpdate: 'system:config:update',
  
  // 版本管理
  versionCreate: 'system:version:create',
  versionDelete: 'system:version:delete',
  versionUpdate: 'system:version:update',

  // 系统公告
  announcementCreate: 'system:announcement:create',
  announcementDelete: 'system:announcement:delete',
  announcementUpdate: 'system:announcement:update',

  // 消息推送
  messageCreate: 'system:message:create',
  messageDelete: 'system:message:delete',
  messageUpdate: 'system:message:update',

  // 文件管理
  fileDelete: 'system:file:delete',
  fileUpload: 'system:file:upload',
  
 
} as const;

/**
 * 系统运维
 */
export const OPS_PERMS = {
  // 任务调度
  jobQuery: 'ops:job:query',
  jobOperation: 'ops:job:operation',
  jobUpdate: 'ops:job:update',
  jobLogQuery: 'ops:job:log:query',
} as const;

export function hasAccessCode(code: string) {
  return useAccess().hasAccessByCodes([code]);
}

export function filterAccessOptions(
  options: OperationOption[],
  accessMap: Record<string, string>,
) {
  const { hasAccessByCodes } = useAccess();
  return options.map((option) => {
    const code = typeof option === 'string' ? option : option.code;
    const accessCode = accessMap[code];
    if (!accessCode) {
      return option;
    }
    const hasOperationAccess = () => hasAccessByCodes([accessCode]);
    if (typeof option === 'string') {
      return {
        code: option,
        show: hasOperationAccess,
      };
    }
    const originShow = option.show;
    return {
      ...option,
      show: (row: any) => {
        if (!hasOperationAccess()) {
          return false;
        }
        return typeof originShow === 'function'
          ? originShow(row)
          : originShow !== false;
      },
    };
  });
}
