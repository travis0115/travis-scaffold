import { acceptHMRUpdate, defineStore } from 'pinia';

/**
 * @zh_CN HTTP 请求全局 loading 状态管理
 * 通过引用计数支持并发请求，所有请求完成后才关闭 loading
 */
export const useRequestLoadingStore = defineStore('core-request-loading', {
  actions: {
    dec() {
      this.count = Math.max(0, this.count - 1);
      if (this.count === 0) {
        this.spinning = false;
      }
    },
    inc() {
      if (this.count === 0) {
        this.spinning = true;
      }
      this.count++;
    },
  },
  state: (): { count: number; spinning: boolean } => ({
    count: 0,
    spinning: false,
  }),
});

const hot = import.meta.hot;
if (hot) {
  hot.accept(acceptHMRUpdate(useRequestLoadingStore, hot));
}
