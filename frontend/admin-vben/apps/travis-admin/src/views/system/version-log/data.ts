import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemVersionLogApi } from '#/api';

import { z } from '#/adapter/form';
import { FILE_FOLDER_IDS } from '#/api';
import { $t } from '#/locales';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

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
          200,
          $t('ui.formRules.maxLength', [$t('system.version.titleField'), 200]),
        ),
    },
    {
      component: 'RichEditor',
      fieldName: 'content',
      label: $t('system.version.content'),
      rules: z
        .string()
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
        imageUploadFolderId: FILE_FOLDER_IDS.VERSION,
        maxHeight: 520,
        minHeight: 280,
      },
    },
    {
      component: 'DatePicker',
      fieldName: 'publishTime',
      label: $t('system.version.publishTime'),
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        style: { width: '100%' },
      },
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('system.version.statusDraft'), value: 0 },
          { label: $t('system.version.statusPublished'), value: 1 },
        ],
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
        options: [
          { label: $t('system.version.statusDraft'), value: 0 },
          { label: $t('system.version.statusPublished'), value: 1 },
        ],
      },
      fieldName: 'status',
      label: $t('system.version.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemVersionLogApi.VersionLog>,
  onStatusChange?: (
    newStatus: number,
    row: SystemVersionLogApi.VersionLog,
  ) => Promise<boolean>,
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
        attrs: {
          beforeChange: onStatusChange,
        },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
        options: [
          { label: $t('system.version.statusDraft'), value: 0 },
          { label: $t('system.version.statusPublished'), value: 1 },
        ],
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
          [{ code: 'preview', text: '预览' }, 'edit', 'delete'],
          {
            delete: SYSTEM_PERMS.versionDelete,
            edit: SYSTEM_PERMS.versionUpdate,
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
