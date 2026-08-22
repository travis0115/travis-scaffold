import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { h } from 'vue';

import { BACKEND_DATETIME_FORMAT, formatDateTime } from '@vben/utils';

import { Button } from 'antdv-next';

import { z } from '#/adapter/form';
import { enableStatusOptions } from '#/utils/business-options';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, OPS_PERMS } from '#/utils/permissions';

export const JOB_SCHEDULE_TYPE_DICT = 'ops_job_schedule_type';
export const JOB_EXECUTION_STATUS_DICT = 'ops_job_execution_status';

export const scheduleTypeOptions = getDictOptions(JOB_SCHEDULE_TYPE_DICT);

const cronMisfirePolicyOptions = [
  { label: 'Quartz 智能策略（SMART_POLICY）', value: 0 },
  { label: '追赶错过的执行（IGNORE_MISFIRES）', value: 1 },
  { label: '立即补执行一次（FIRE_AND_PROCEED）', value: 2 },
  { label: '跳过错过的执行，等待下一次（DO_NOTHING）', value: 3 },
];

const simpleMisfirePolicyOptions = [
  { label: 'Quartz 智能策略（SMART_POLICY）', value: 0 },
  { label: '追赶错过的执行（IGNORE_MISFIRES）', value: 1 },
  { label: '立即补执行一次（FIRE_NOW）', value: 2 },
  {
    label: '跳过错过的执行，等待下一次（NEXT_WITH_REMAINING_COUNT）',
    value: 3,
  },
];

const requiredString = (message: string, max: number, maxMessage: string) =>
  z
    .string({ invalid_type_error: message, required_error: message })
    .trim()
    .min(1, message)
    .max(max, maxMessage);

const requiredNumber = (message: string) =>
  z.number({ invalid_type_error: message, required_error: message });

export function useJobFormSchema(
  onOpenCronGenerator: () => void,
): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'jobName',
      label: '任务名称',
      rules: requiredString(
        '请输入任务名称',
        120,
        '任务名称长度不能超过120个字符',
      ),
    },
    {
      component: 'Select',
      componentProps: { options: [] },
      fieldName: 'handlerName',
      label: '任务处理器',
      rules: requiredString(
        '请选择任务处理器',
        120,
        '任务处理器长度不能超过120个字符',
      ),
    },
    {
      component: 'RadioGroup',
      componentProps: { options: scheduleTypeOptions },
      defaultValue: 'CRON',
      fieldName: 'scheduleType',
      label: '调度类型',
      rules: z
        .string({
          invalid_type_error: '请选择调度类型',
          required_error: '请选择调度类型',
        })
        .refine(
          (value) => ['CRON', 'INTERVAL', 'ONCE'].includes(value),
          '调度类型不正确',
        ),
    },
    {
      component: 'Input',
      dependencies: {
        required: (values) => values.scheduleType === 'CRON',
        rules: (values) =>
          values.scheduleType === 'CRON'
            ? requiredString(
                '请输入 Cron 表达式',
                120,
                'Cron 表达式长度不能超过120个字符',
              )
            : null,
        show: (values) => values.scheduleType === 'CRON',
        trigger: (_values, actions) =>
          actions.setFieldError('cronExpression', undefined),
        triggerFields: ['scheduleType'],
      },
      fieldName: 'cronExpression',
      label: 'Cron 表达式',
      renderComponentContent: () => ({
        addonAfter: () =>
          h(
            Button,
            {
              onClick: onOpenCronGenerator,
              size: 'small',
              type: 'link',
            },
            () => 'Cron 生成器',
          ),
      }),
    },
    {
      component: 'InputNumber',
      componentProps: { min: 1, step: 1, class: 'w-full' },
      defaultValue: 5,
      dependencies: {
        required: (values) => values.scheduleType === 'INTERVAL',
        rules: (values) =>
          values.scheduleType === 'INTERVAL'
            ? requiredNumber('请输入固定间隔')
                .int('固定间隔必须是整数')
                .min(1, '固定间隔不能小于1秒')
            : null,
        show: (values) => values.scheduleType === 'INTERVAL',
        trigger: (_values, actions) =>
          actions.setFieldError('intervalSeconds', undefined),
        triggerFields: ['scheduleType'],
      },
      fieldName: 'intervalSeconds',
      label: '固定间隔（秒）',
    },
    {
      component: 'DatePicker',
      componentProps: {
        class: 'w-full',
        showTime: true,
        valueFormat: BACKEND_DATETIME_FORMAT,
      },
      dependencies: {
        required: (values) => values.scheduleType === 'ONCE',
        rules: (values) =>
          values.scheduleType === 'ONCE'
            ? z
                .string({
                  invalid_type_error: '请选择执行时间',
                  required_error: '请选择执行时间',
                })
                .min(1, '请选择执行时间')
            : null,
        show: (values) => values.scheduleType === 'ONCE',
        trigger: (_values, actions) =>
          actions.setFieldError('executeAt', undefined),
        triggerFields: ['scheduleType'],
      },
      fieldName: 'executeAt',
      formFieldProps: {
        validateOnBlur: false,
        validateOnChange: true,
      },
      label: '执行时间',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '禁止并发', value: 0 },
          { label: '允许并发', value: 1 },
        ],
      },
      defaultValue: 0,
      fieldName: 'concurrent',
      label: '并发策略',
      rules: requiredNumber('请选择并发策略').refine(
        (value) => [0, 1].includes(value),
        '并发策略不正确',
      ),
    },
    {
      component: 'Select',
      componentProps: (values) => ({
        options:
          values.scheduleType === 'CRON'
            ? cronMisfirePolicyOptions
            : simpleMisfirePolicyOptions,
      }),
      defaultValue: 0,
      fieldName: 'misfirePolicy',
      label: 'Misfire 策略',
      rules: requiredNumber('请选择 Misfire 策略').refine(
        (value) => [0, 1, 2, 3].includes(value),
        'Misfire 策略不正确',
      ),
    },
    {
      component: 'Textarea',
      componentProps: { rows: 5 },
      defaultValue: '{}',
      fieldName: 'params',
      label: 'JSON 参数',
      rules: z.string({ invalid_type_error: 'JSON 参数格式不正确' }).optional(),
    },
    {
      component: 'Select',
      componentProps: { mode: 'multiple', options: [] },
      fieldName: 'alertUserIds',
      label: '失败告警接收人',
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: '备注',
      rules: z.string().max(500, '备注长度不能超过500个字符').optional(),
    },
  ];
}

export const useJobGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'jobName', label: '任务名称' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      optionFilterProp: 'label',
      options: [],
      showSearch: true,
    },
    fieldName: 'handlerName',
    label: '处理器',
  },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: scheduleTypeOptions },
    fieldName: 'scheduleType',
    label: '调度类型',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: enableStatusOptions.toSorted(
        (left, right) => Number(right.value) - Number(left.value),
      ),
    },
    fieldName: 'status',
    label: '状态',
  },
];

export function useJobColumns(
  onActionClick: OnActionClickFn<OpsJobApi.Job>,
  onStatusChange?: (value: number, row: OpsJobApi.Job) => Promise<boolean>,
): VxeTableGridColumns {
  const isBuiltin = (row: Pick<OpsJobApi.Job, 'isBuiltin'>) =>
    row.isBuiltin === 1;

  return [
    {
      field: 'jobName',
      minWidth: 180,
      slots: { default: 'jobName' },
      title: '任务名称',
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_config_type' },
        name: 'CellTag',
      },
      field: 'isBuiltin',
      title: '任务类型',
      width: 110,
    },
    {
      field: 'createByUsername',
      formatter: ({ cellValue, row }: any) => cellValue || row.createBy || '-',
      title: '创建人',
      width: 120,
    },
    {
      field: 'handlerName',
      formatter: ({ cellValue, row }: any) =>
        row.handlerAvailable === false ? `${cellValue}（未上线）` : cellValue,
      minWidth: 210,
      title: '处理器',
    },
    {
      cellRender: {
        attrs: { dictCode: JOB_SCHEDULE_TYPE_DICT },
        name: 'CellTag',
      },
      field: 'scheduleType',
      title: '类型',
      width: 110,
    },
    {
      field: 'cronExpression',
      formatter: ({ cellValue, row }: any) => {
        if (row.scheduleType !== 'INTERVAL') return cellValue || '-';
        const seconds = Number(row.intervalMillis) / 1000;
        return Number.isFinite(seconds) ? `${seconds} 秒` : '-';
      },
      minWidth: 150,
      title: 'Cron / 间隔时间',
    },
    {
      field: 'nextFireTime',
      showOverflow: false,
      slots: { default: 'executionInfo' },
      title: '执行信息',
      width: 280,
    },
    {
      cellRender: {
        attrs: {
          beforeChange: onStatusChange,
          dictCode: 'enable_status',
          disabled: isBuiltin,
        },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: '状态',
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'jobName',
          nameTitle: '任务',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            {
              code: 'stats',
              text: '统计',
            },
            {
              code: 'edit',
              show: (row: OpsJobApi.Job) => !isBuiltin(row),
            },
            {
              code: 'delete',
              show: (row: OpsJobApi.Job) => !isBuiltin(row),
            },
            {
              breakBefore: (row: OpsJobApi.Job) => !isBuiltin(row),
              code: 'run',
              text: '执行',
            },
            {
              code: 'copy',
              show: (row: OpsJobApi.Job) => !isBuiltin(row),
              text: '复制',
            },
          ],
          {
            copy: OPS_PERMS.jobUpdate,
            delete: OPS_PERMS.jobUpdate,
            edit: OPS_PERMS.jobUpdate,
            run: OPS_PERMS.jobOperation,
            stats: OPS_PERMS.jobQuery,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      showOverflow: false,
      title: '操作',
      width: 210,
    },
  ];
}

export const useLogGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'jobName', label: '任务名称' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      optionFilterProp: 'label',
      options: [],
      showSearch: true,
    },
    fieldName: 'handlerName',
    label: '处理器',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: getDictOptions(JOB_EXECUTION_STATUS_DICT),
    },
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'RangePicker',
    componentProps: {
      showTime: true,
      valueFormat: BACKEND_DATETIME_FORMAT,
    },
    fieldName: 'executionTimeRange',
    label: '开始时间',
  },
];

export function useLogColumns(
  onActionClick: OnActionClickFn<OpsJobApi.JobLogPage>,
): VxeTableGridColumns {
  return [
    { field: 'jobName', minWidth: 180, title: '任务名称' },
    { field: 'handlerName', minWidth: 160, title: '处理器' },
    { field: 'schedulerInstanceId', minWidth: 170, title: '执行实例' },
    {
      field: 'startTime',
      formatter: 'formatDateTime',
      title: '开始时间',
      width: 180,
    },
    {
      field: 'endTime',
      formatter: ({ cellValue }: any) =>
        cellValue ? formatDateTime(cellValue) : '-',
      title: '结束时间',
      width: 180,
    },
    {
      field: 'durationMillis',
      fixed: 'right',
      formatter: ({ cellValue }: any) => cellValue ?? '-',
      title: '耗时（ms）',
      width: 110,
    },
    {
      cellRender: {
        attrs: { dictCode: JOB_EXECUTION_STATUS_DICT },
        name: 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: '状态',
      width: 90,
    },
    {
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: filterAccessOptions([{ code: 'detail', text: '详情' }], {
          detail: OPS_PERMS.jobLogQuery,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 90,
    },
  ];
}
