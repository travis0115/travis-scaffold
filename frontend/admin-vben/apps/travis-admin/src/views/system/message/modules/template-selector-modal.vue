<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getMessageTemplateDetail, getMessageTemplatePage } from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';

const emit = defineEmits<{
  success: [template?: SystemMessageApi.MessageTemplate];
}>();

const channel = ref('IN_APP');
const previewTemplate = ref<SystemMessageApi.MessageTemplate>();
const selectedId = ref<SystemMessageApi.Id>();
const emptyValueFormatter = ({ cellValue }: { cellValue: unknown }) =>
  cellValue === undefined || cellValue === null || cellValue === ''
    ? '-'
    : String(cellValue);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        component: 'Input',
        fieldName: 'templateName',
        label: '模板名称',
      },
      {
        component: 'Input',
        fieldName: 'templateCode',
        label: '模板编码',
      },
    ],
    wrapperClass: 'grid-cols-2',
  },
  gridEvents: {
    cellClick: ({ row }: { row: SystemMessageApi.MessageTemplate }) => {
      previewTemplate.value = row;
      gridApi.grid.setCurrentRow(row);
    },
  },
  gridOptions: {
    columns: [
      {
        field: 'templateName',
        formatter: emptyValueFormatter,
        minWidth: 150,
        title: '模板名称',
      },
      {
        field: 'templateCode',
        formatter: emptyValueFormatter,
        minWidth: 140,
        title: '模板编码',
      },
      {
        field: 'action',
        fixed: 'right',
        slots: { default: 'action' },
        title: '操作',
        width: 76,
      },
    ],
    height: '100%',
    pagerConfig: { pageSize: 20 },
    proxyConfig: {
      autoLoad: false,
      ajax: {
        query: async ({ page }, values) => {
          const result = await getMessageTemplatePage({
            channel: channel.value,
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            status: 1,
            ...values,
          });
          const current =
            result.records.find((item) => item.id === selectedId.value) ??
            result.records[0];
          previewTemplate.value = current;
          await nextTick();
          if (current) gridApi.grid.setCurrentRow(current);
          return result;
        },
      },
    },
    rowConfig: { isCurrent: true, keyField: 'id' },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<SystemMessageApi.MessageTemplate>,
});

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(open) {
    if (!open) return;
    const data = modalApi.getData<{
      channel: string;
      selectedId?: SystemMessageApi.Id;
    }>();
    channel.value = data.channel;
    selectedId.value = data.selectedId;
    previewTemplate.value = data.selectedId
      ? await getMessageTemplateDetail(data.selectedId)
      : undefined;
  },
  async onOpened() {
    await nextTick();
    await gridApi.query();
  },
  zIndex: 2200,
});

async function selectTemplate(template?: SystemMessageApi.MessageTemplate) {
  emit(
    'success',
    template ? await getMessageTemplateDetail(template.id) : undefined,
  );
  modalApi.close();
}
</script>

<template>
  <Modal
    class="h-[760px] w-[1200px]"
    content-class="min-h-0 overflow-hidden"
    title="消息模板"
  >
    <div class="flex h-full min-h-0 gap-4">
      <div class="min-w-0 flex-[3]">
        <Grid>
          <template #action="{ row }">
            <Button
              class="transition-colors"
              size="small"
              type="link"
              @click.stop="selectTemplate(row)"
            >
              选择
            </Button>
          </template>
        </Grid>
      </div>
      <div class="min-w-0 flex-[2] overflow-auto rounded border p-4">
        <template v-if="previewTemplate">
          <div class="mb-3 border-b pb-3">
            <div class="text-base font-semibold">
              {{ previewTemplate.title || previewTemplate.templateName }}
            </div>
          </div>
          <RichTextPreview :content="previewTemplate.content" :min-height="420" />
        </template>
        <div v-else class="flex h-full items-center justify-center text-gray-400">
          暂无可预览模板
        </div>
      </div>
    </div>
  </Modal>
</template>
