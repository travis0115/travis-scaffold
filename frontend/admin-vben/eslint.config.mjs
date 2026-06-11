import { defineConfig } from '@vben/eslint-config';

export default defineConfig([
  {
    files: ['apps/travis-admin/src/**/*.{ts,vue}'],
    rules: {
      'no-restricted-properties': [
        'error',
        {
          message:
            '普通文本请使用 textContent，富文本必须通过统一的安全组件渲染。',
          property: 'innerHTML',
        },
        {
          message: '禁止直接使用 outerHTML。',
          property: 'outerHTML',
        },
        {
          message: '禁止直接插入未经清洗的 HTML。',
          property: 'insertAdjacentHTML',
        },
      ],
      'vue/no-v-html': 'error',
    },
  },
]);
