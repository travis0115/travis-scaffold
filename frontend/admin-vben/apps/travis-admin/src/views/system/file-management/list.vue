<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemFileApi } from '#/api';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Form, FormItem, Image, Input, message, Modal, RadioGroup, Select, Upload } from 'antdv-next';

import { InputNumber } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createFileFolder,
  createStorageConfig,
  deleteFile,
  getFileFolders,
  getFilePage,
  getStorageConfigs,
  uploadFileApi,
} from '#/api';

import { useColumns, useGridFormSchema } from './data';

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getFilePage({ pageNum: page.currentPage, pageSize: page.pageSize, ...values }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemFileApi.FileInfo>,
});

const folders = ref<SystemFileApi.Folder[]>([]);
const storageConfigs = ref<SystemFileApi.StorageConfig[]>([]);
const folderId = ref<number>();
const folderModalOpen = ref(false);
const storageModalOpen = ref(false);
const folderForm = ref({ folderName: '', parentId: 0, sort: 0 });
const storageForm = ref({
  accessPrefix: '/files',
  basePath: '${user.home}/data/uploads',
  configName: '',
  domain: '',
  isDefault: 0,
  status: 1,
  storageType: 'LOCAL',
});

async function loadOptions() {
  [folders.value, storageConfigs.value] = await Promise.all([
    getFileFolders(),
    getStorageConfigs(),
  ]);
}

function onActionClick({ code, row }: OnActionClickParams<SystemFileApi.FileInfo>) {
  if (code === 'delete') {
    deleteFile(row.id).then(() => gridApi.query());
  }
}

async function customRequest({ file }: any) {
  await uploadFileApi(file, folderId.value);
  message.success('上传成功');
  gridApi.query();
}

async function saveFolder() {
  await createFileFolder(folderForm.value);
  folderModalOpen.value = false;
  folderForm.value = { folderName: '', parentId: 0, sort: 0 };
  await loadOptions();
}

async function saveStorage() {
  await createStorageConfig(storageForm.value);
  storageModalOpen.value = false;
  await loadOptions();
}

onMounted(loadOptions);
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="文件管理">
      <template #toolbar-tools>
        <Select
          v-model:value="folderId"
          allow-clear
          class="w-48"
          placeholder="选择文件夹"
          :options="folders.map((item) => ({ label: item.folderName, value: item.id }))"
        />
        <Button @click="folderModalOpen = true">新增文件夹</Button>
        <Button @click="storageModalOpen = true">
          存储配置（{{ storageConfigs.length }}）
        </Button>
        <Upload :custom-request="customRequest" :show-upload-list="false">
          <Button type="primary">上传文件</Button>
        </Upload>
      </template>
      <template #preview="{ row }">
        <Image
          v-if="row.mimeType?.startsWith('image/')"
          :src="row.url"
          :width="48"
          :height="48"
          class="object-cover"
        />
        <Button v-else type="link" :href="row.url" target="_blank">
          预览
        </Button>
      </template>
    </Grid>
    <Modal v-model:open="folderModalOpen" title="新增文件夹" @ok="saveFolder">
      <Form layout="vertical">
        <FormItem label="文件夹名称"><Input v-model:value="folderForm.folderName" /></FormItem>
        <FormItem label="排序"><InputNumber v-model:value="folderForm.sort" /></FormItem>
      </Form>
    </Modal>
    <Modal v-model:open="storageModalOpen" title="新增存储配置" @ok="saveStorage">
      <Form layout="vertical">
        <FormItem label="配置名称"><Input v-model:value="storageForm.configName" /></FormItem>
        <FormItem label="存储类型"><Input v-model:value="storageForm.storageType" disabled /></FormItem>
        <FormItem label="存储目录"><Input v-model:value="storageForm.basePath" /></FormItem>
        <FormItem label="访问前缀"><Input v-model:value="storageForm.accessPrefix" /></FormItem>
        <FormItem label="访问域名"><Input v-model:value="storageForm.domain" /></FormItem>
        <FormItem label="设为默认">
          <RadioGroup
            v-model:value="storageForm.isDefault"
            :options="[{ label: '否', value: 0 }, { label: '是', value: 1 }]"
          />
        </FormItem>
      </Form>
    </Modal>
  </Page>
</template>
