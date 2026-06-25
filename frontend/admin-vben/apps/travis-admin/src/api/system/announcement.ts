import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemAnnouncementApi {
  export interface Announcement {
    content: string;
    createTime?: string;
    expireTime?: string;
    id: number;
    pinned?: number;
    publishTime?: string;
    remark?: string;
    sort?: number;
    status: number;
    title: string;
  }
}

const getAnnouncementPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemAnnouncementApi.Announcement>>('/system/announcement/page', {
    params,
  });
const getAnnouncementDetail = (id: number) =>
  requestClient.get<SystemAnnouncementApi.Announcement>(`/system/announcement/${id}`);
const createAnnouncement = (data: Partial<SystemAnnouncementApi.Announcement>) =>
  requestClient.post('/system/announcement', data);
const updateAnnouncement = (id: number, data: Partial<SystemAnnouncementApi.Announcement>) =>
  requestClient.put(`/system/announcement/${id}`, data);
const updateAnnouncementStatus = (id: number, status: number) =>
  requestClient.put(`/system/announcement/${id}/status`, undefined, { params: { status } });
const deleteAnnouncement = (id: number) => requestClient.delete(`/system/announcement/${id}`);

export {
  createAnnouncement,
  deleteAnnouncement,
  getAnnouncementDetail,
  getAnnouncementPage,
  updateAnnouncement,
  updateAnnouncementStatus,
};
