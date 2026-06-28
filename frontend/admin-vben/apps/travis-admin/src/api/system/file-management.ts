import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemFileApi {
  export interface FileInfo {
    [key: string]: any;
    createBy?: number | string;
    createTime: string;
    extension?: string;
    fileName: string;
    id: number | string;
    mimeType?: string;
    originalName: string;
    path: string;
    size: number;
    storageConfigId: number;
    storageConfigName?: string;
    storageType?: string;
    uploaderName?: string;
    uploaderType?: string;
    url: string;
  }

  export interface Folder {
    children?: Folder[];
    folderName: string;
    id: number | string;
    isBuiltin?: 0 | 1;
    parentId?: number | string;
    sort?: number;
  }

  export interface StorageConfig {
    accessKey?: string;
    bucketId?: string;
    bucketName?: string;
    configName: string;
    domain?: string;
    endpoint?: string;
    id: number | string;
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
const deleteFile = (id: number | string) =>
  requestClient.delete(`/system/file/${id}`);
const getUploadPolicy = () =>
  requestClient.get<SystemFileApi.UploadPolicy>('/system/file/upload-policy');
const getFileFolders = () =>
  requestClient.get<SystemFileApi.Folder[]>('/system/file/folder/list');
const createFileFolder = (data: Partial<SystemFileApi.Folder>) =>
  requestClient.post('/system/file/folder', data);
const updateFileFolder = (
  id: number | string,
  data: Partial<SystemFileApi.Folder>,
) => requestClient.put(`/system/file/folder/${id}`, data);
const deleteFileFolder = (id: number | string) =>
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
const getStorageConfigDetail = (id: number | string) =>
  requestClient.get<SystemFileApi.StorageConfig>(`/system/file/storage/${id}`);
const createStorageConfig = (data: Partial<SystemFileApi.StorageConfig>) =>
  requestClient.post('/system/file/storage', data);
const updateStorageConfig = (
  id: number | string,
  data: Partial<SystemFileApi.StorageConfig>,
) => requestClient.put(`/system/file/storage/${id}`, data);
const updateStorageConfigStatus = (id: number | string, status: number) =>
  requestClient.put(`/system/file/storage/${id}/status`, undefined, {
    params: { status },
  });
const setDefaultStorageConfig = (id: number | string) =>
  requestClient.put(`/system/file/storage/${id}/default`);
const deleteStorageConfig = (id: number | string) =>
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
  updateStorageConfigStatus,
};
