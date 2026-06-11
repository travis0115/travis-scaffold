import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemFileApi {
  export interface FileInfo {
    [key: string]: any;
    createTime: string;
    createBy?: number;
    creatorName?: string;
    extension?: string;
    fileName: string;
    id: number;
    mimeType?: string;
    originalName: string;
    path: string;
    size: number;
    storageConfigId: number;
    storageConfigName?: string;
    storageType?: string;
    url: string;
  }

  export interface Folder {
    folderName: string;
    id: number;
    parentId?: number;
    sort?: number;
  }

  export interface StorageConfig {
    accessPrefix: string;
    basePath: string;
    configName: string;
    domain?: string;
    id: number;
    isDefault: number;
    status: number;
    storageType: string;
  }
}

const getFilePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemFileApi.FileInfo>>('/system/file/page', { params });
const deleteFile = (id: number) => requestClient.delete(`/system/file/${id}`);
const getFileFolders = () =>
  requestClient.get<SystemFileApi.Folder[]>('/system/file-folder/list');
const createFileFolder = (data: Partial<SystemFileApi.Folder>) =>
  requestClient.post('/system/file-folder', data);
const getStorageConfigs = () =>
  requestClient.get<SystemFileApi.StorageConfig[]>('/system/file-storage/list');
const createStorageConfig = (data: Partial<SystemFileApi.StorageConfig>) =>
  requestClient.post('/system/file-storage', data);

export {
  createFileFolder,
  createStorageConfig,
  deleteFile,
  getFileFolders,
  getFilePage,
  getStorageConfigs,
};
