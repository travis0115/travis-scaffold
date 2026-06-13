import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { z } from '#/adapter/form';

export const scheduleTypeOptions = [
  { label: 'Cron 表达式', value: 'CRON' },
  { label: '固定间隔', value: 'INTERVAL' },
  { label: '单次执行', value: 'ONCE' },
];

export function useJobFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'jobName',
      label: '任务名称',
      rules: z.string().min(1),
    },
    {
      component: 'Select',
      componentProps: { options: [] },
      fieldName: 'handlerName',
      label: '任务处理器',
      rules: z.string().min(1),
    },
    {
      component: 'RadioGroup',
      componentProps: { options: scheduleTypeOptions },
      defaultValue: 'CRON',
      fieldName: 'scheduleType',
      label: '调度类型',
    },
    {
      component: 'Input',
      dependencies: {
        show: (values) => values.scheduleType === 'CRON',
        triggerFields: ['scheduleType'],
      },
      fieldName: 'cronExpression',
      label: 'Cron 表达式',
    },
    {
      component: 'InputNumber',
      componentProps: { min: 1000, step: 1000, class: 'w-full' },
      dependencies: {
        show: (values) => values.scheduleType === 'INTERVAL',
        triggerFields: ['scheduleType'],
      },
      fieldName: 'intervalMillis',
      label: '固定间隔（毫秒）',
    },
    {
      component: 'DatePicker',
      componentProps: {
        class: 'w-full',
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
      dependencies: {
        show: (values) => values.scheduleType === 'ONCE',
        triggerFields: ['scheduleType'],
      },
      fieldName: 'executeAt',
      label: '执行时间',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', max: 10, min: 1 },
      defaultValue: 5,
      fieldName: 'priority',
      label: '优先级',
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
    },
    {
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Quartz 智能策略', value: 0 },
          { label: '忽略错过执行', value: 1 },
          { label: '立即补执行一次', value: 2 },
          { label: '等待下一次执行', value: 3 },
        ],
      },
      defaultValue: 0,
      fieldName: 'misfirePolicy',
      label: 'Misfire 策略',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 5 },
      defaultValue: '{}',
      fieldName: 'params',
      label: 'JSON 参数',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 7 },
      fieldName: 'paramSchema',
      label: 'JSON Schema',
    },
    {
      component: 'Select',
      componentProps: {
        mode: 'multiple',
        options: [
          { label: '星期日', value: 1 },
          { label: '星期一', value: 2 },
          { label: '星期二', value: 3 },
          { label: '星期三', value: 4 },
          { label: '星期四', value: 5 },
          { label: '星期五', value: 6 },
          { label: '星期六', value: 7 },
        ],
      },
      fieldName: 'excludedWeekdays',
      label: '排除星期',
    },
    {
      component: 'Input',
      fieldName: 'excludedDatesText',
      label: '排除日期',
      componentProps: {
        placeholder: '多个日期用逗号分隔，例如 2026-01-01,2026-02-17',
      },
    },
    {
      component: 'Input',
      fieldName: 'dailyStartTime',
      label: '每日开始时间',
      componentProps: { placeholder: 'HH:mm:ss' },
    },
    {
      component: 'Input',
      fieldName: 'dailyEndTime',
      label: '每日结束时间',
      componentProps: { placeholder: 'HH:mm:ss' },
    },
    {
      component: 'Select',
      componentProps: { allowClear: true, options: [] },
      fieldName: 'ownerUserId',
      label: '负责人',
    },
    {
      component: 'Select',
      componentProps: { mode: 'multiple', options: [] },
      fieldName: 'alertUserIds',
      label: '失败告警接收人',
    },
    {
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      defaultValue: 30,
      fieldName: 'logRetentionDays',
      label: '日志保留天数',
    },
    { component: 'Textarea', fieldName: 'remark', label: '备注' },
  ];
}

export const useJobGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'jobName', label: '任务名称' },
  { component: 'Input', fieldName: 'handlerName', label: '处理器' },
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
      options: [
        { label: '暂停', value: 0 },
        { label: '启用', value: 1 },
      ],
    },
    fieldName: 'status',
    label: '状态',
  },
];

export function useJobColumns(
  onActionClick: OnActionClickFn<OpsJobApi.Job>,
  onStatusChange: (value: number, row: OpsJobApi.Job) => Promise<boolean>,
): VxeTableGridColumns {
  return [
    { field: 'jobName', minWidth: 180, title: '任务名称' },
    {
      field: 'handlerName',
      formatter: ({ cellValue, row }: any) =>
        row.handlerAvailable === false ? `${cellValue}（未上线）` : cellValue,
      minWidth: 190,
      title: '处理器',
    },
    {
      field: 'scheduleType',
      formatter: ({ cellValue }: any) => {
        const labels: Record<string, string> = {
          CRON: 'Cron',
          INTERVAL: '固定间隔',
          ONCE: '单次',
        };
        return labels[String(cellValue)] ?? cellValue;
      },
      title: '类型',
      width: 100,
    },
    { field: 'cronExpression', minWidth: 150, title: 'Cron' },
    { field: 'ownerUsername', title: '负责人', width: 120 },
    {
      field: 'nextFireTime',
      formatter: 'formatDateTime',
      title: '下次执行',
      width: 180,
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: 'CellSwitch',
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
        options: [
          'edit',
          { code: 'run', text: '执行' },
          { code: 'stats', text: '统计' },
          { code: 'copy', text: '复制' },
          'delete',
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 280,
    },
  ];
}

export const useLogGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'jobName', label: '任务名称' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: [
        { label: '执行中', value: 0 },
        { label: '成功', value: 1 },
        { label: '失败', value: 2 },
      ],
    },
    fieldName: 'status',
    label: '状态',
  },
];

export function useLogColumns(
  onActionClick: OnActionClickFn<OpsJobApi.JobLog>,
): VxeTableGridColumns {
  return [
    { field: 'jobName', minWidth: 180, title: '任务名称' },
    { field: 'handlerName', minWidth: 160, title: '处理器' },
    { field: 'schedulerInstanceId', minWidth: 170, title: '执行实例' },
    {
      field: 'status',
      formatter: ({ cellValue }: any) =>
        ['执行中', '成功', '失败'][cellValue] ?? '-',
      title: '状态',
      width: 90,
    },
    { field: 'durationMillis', title: '耗时（ms）', width: 110 },
    {
      field: 'startTime',
      formatter: 'formatDateTime',
      title: '开始时间',
      width: 180,
    },
    { field: 'exceptionMessage', minWidth: 200, title: '异常信息' },
    {
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [{ code: 'detail', text: '详情' }],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 90,
    },
  ];
}
