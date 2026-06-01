import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    name: import.meta.env.VITE_APP_TITLE,
    accessMode: 'backend',
    defaultAvatar: '/static/img/default-avatar.png',
    defaultHomePath: '/dashboard',
    enablePreferences: true,
  },
  breadcrumb: {
    enable: false,
  },
  copyright: {
    companyName: 'Muxi AI Labs',
    companySiteLink: 'https://github.com/travis0115',
    date: '2026',
  },
  footer: {
    enable: false,
  },
  logo: {
    enable: true,
    fit: 'contain',
    source: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
    // sourceDark: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-dark.webp', // 可选：暗色主题logo
  },
  navigation: {
    accordion: false,
  },
  shortcutKeys: {
    enable: false,
    globalLockScreen: false,
    globalLogout: false,
    globalSearch: false,
  },
  sidebar: {
    collapsedShowTitle: false,
    fixedButton: false,
  },
  theme: {
    builtinType: 'violet',
    colorPrimary: 'hsl(245 82% 67%)',
    mode: 'light',
  },
  widget: {
    globalSearch: false,
    languageToggle: false,
    lockScreen: false,
    notification: false,
    sidebarToggle: false,
    timezone: false,
  },
});
