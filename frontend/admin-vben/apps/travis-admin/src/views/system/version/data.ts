import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemVersionLogApi } from '#/api';

import { z } from '#/adapter/form';
import { uploadVersionLogImage } from '#/api';
import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

const publishStatusOptions = getDictOptions('sys_publish_status');

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

function hasRichTextContent(value?: string) {
  if (!value) return false;
  return (
    value
      .replaceAll('&nbsp;', ' ')
      .replaceAll(/<br\s*\/?>/gi, '')
      .replaceAll(/<[^>]*>/g, '')
      .trim().length > 0
  );
}

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.version.version'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.version.version')]))
        .max(
          50,
          $t('ui.formRules.maxLength', [$t('system.version.version'), 50]),
        ),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.version.titleField'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.version.titleField')]))
        .max(
          255,
          $t('ui.formRules.maxLength', [$t('system.version.titleField'), 255]),
        ),
    },
    {
      component: 'RichEditor',
      fieldName: 'content',
      label: $t('system.version.content'),
      rules: z
        .string()
        .max(5000, '版本内容长度不能超过5000个字符')
        .refine(
          hasRichTextContent,
          $t('ui.formRules.required', [$t('system.version.content')]),
        ),
      formFieldProps: {
        validateOnBlur: false,
        validateOnChange: false,
        validateOnInput: false,
        validateOnModelUpdate: false,
      },
      componentProps: {
        imageUpload: {
          upload: async (
            file: File,
            onProgress?: (percent: number) => void,
          ) => {
            const result = await uploadVersionLogImage(file, (event) => {
              if (!event.total) return;
              onProgress?.(Math.round((event.loaded / event.total) * 100));
            });
            return { id: result.id, url: result.url };
          },
        },
        maxHeight: 520,
        minHeight: 280,
      },
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: publishStatusOptions.filter(
          (option) => Number(option.value) !== 2,
        ),
        optionType: 'button',
      },
      defaultValue: 0,
      fieldName: 'status',
      label: $t('system.version.status'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.version.version'),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.version.titleField'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: publishStatusOptions.filter(
          (option) => Number(option.value) !== 2,
        ),
      },
      fieldName: 'status',
      label: $t('system.version.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemVersionLogApi.VersionLog>,
): VxeTableGridColumns<SystemVersionLogApi.VersionLog> {
  return [
    {
      field: 'version',
      title: $t('system.version.version'),
      formatter: ({ cellValue }) => formatVersion(cellValue),
      width: 120,
    },
    {
      field: 'title',
      minWidth: 200,
      slots: { default: 'title' },
      title: $t('system.version.titleField'),
    },
    {
      field: 'publishTime',
      title: $t('system.version.publishTime'),
      width: 180,
      formatter: 'formatDateTime',
      sortable: true,
    },
    {
      field: 'createTime',
      title: $t('system.version.createTime'),
      width: 180,
      formatter: 'formatDateTime',
      sortable: true,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_publish_status' },
        name: 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.version.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: $t('system.version.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            { code: 'preview', text: '预览' },
            {
              code: 'publish',
              show: (row: SystemVersionLogApi.VersionLog) => row.status !== 1,
              text: '发布',
            },
            { code: 'edit' },
            { code: 'delete' },
          ],
          {
            delete: SYSTEM_PERMS.versionDelete,
            edit: SYSTEM_PERMS.versionUpdate,
            publish: SYSTEM_PERMS.versionUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.version.operation'),
      width: 220,
    },
  ];
}
