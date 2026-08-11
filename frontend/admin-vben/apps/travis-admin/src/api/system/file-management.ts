import type { Recordable } from '@vben/types';

import type { Id, PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemFileApi {
  export interface FileInfo {
    [key: string]: any;
    createTime: string;
    extension?: string;
    fileName: string;
    id: Id;
    mimeType?: string;
    originalName: string;
    path: string;
    size: number;
    storageConfigId: Id;
    storageConfigName?: string;
    storageType?: string;
    uploaderId?: Id;
    uploaderName?: string;
    uploaderType?: string;
    url: string;
  }

  export interface Folder {
    children?: Folder[];
    folderName: string;
    id: Id;
    isBuiltin?: 0 | 1;
    parentId?: Id;
    sort?: number;
  }

  export interface StorageConfig {
    accessKey?: string;
    bucketId?: string;
    bucketName?: string;
    configName: string;
    domain?: string;
    endpoint?: string;
    id: Id;
    isDefault: number;
    meta?: string;
    region?: string;
    remark?: string;
    secretKey?: string;
    status: number;
    storagePath: string;
    storageType: string;
  }

  export interface StorageTypeOption {
    label: string;
    value: string;
  }

  export interface UploadPolicy {
    allowedExtensions: string[];
    maxFileSizeBytes: number;
  }
}

const getFilePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemFileApi.FileInfo>>('/system/file/page', {
    params,
  });
const deleteFile = (id: Id) => requestClient.delete(`/system/file/${id}`);
const getUploadPolicy = () =>
  requestClient.get<SystemFileApi.UploadPolicy>('/system/file/upload-policy');
const getFileFolders = () =>
  requestClient.get<SystemFileApi.Folder[]>('/system/file/folder/list');
const createFileFolder = (data: Partial<SystemFileApi.Folder>) =>
  requestClient.post('/system/file/folder', data);
const updateFileFolder = (id: Id, data: Partial<SystemFileApi.Folder>) =>
  requestClient.put(`/system/file/folder/${id}`, data);
const deleteFileFolder = (id: Id) =>
  requestClient.delete(`/system/file/folder/${id}`);
const getStorageConfigs = () =>
  requestClient.get<SystemFileApi.StorageConfig[]>('/system/file/storage/list');
const getStorageTypes = () =>
  requestClient.get<SystemFileApi.StorageTypeOption[]>(
    '/system/file/storage/types',
  );
const getStorageConfigPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemFileApi.StorageConfig>>(
    '/system/file/storage/page',
    { params },
  );
const getStorageConfigDetail = (id: Id) =>
  requestClient.get<SystemFileApi.StorageConfig>(`/system/file/storage/${id}`);
const createStorageConfig = (data: Partial<SystemFileApi.StorageConfig>) =>
  requestClient.post('/system/file/storage', data);
const updateStorageConfig = (
  id: Id,
  data: Partial<SystemFileApi.StorageConfig>,
) => requestClient.put(`/system/file/storage/${id}`, data);
const setDefaultStorageConfig = (id: Id) =>
  requestClient.put(`/system/file/storage/${id}/default`);
const deleteStorageConfig = (id: Id) =>
  requestClient.delete(`/system/file/storage/${id}`);

export {
  createFileFolder,
  createStorageConfig,
  deleteFile,
  deleteFileFolder,
  deleteStorageConfig,
  getFileFolders,
  getFilePage,
  getStorageConfigDetail,
  getStorageConfigPage,
  getStorageConfigs,
  getStorageTypes,
  getUploadPolicy,
  setDefaultStorageConfig,
  updateFileFolder,
  updateStorageConfig,
};
