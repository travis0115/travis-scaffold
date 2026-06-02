import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            // mock代理目标地址
            target: 'http://localhost',
            ws: true,
          },
        },
      },
    },
  };
});
