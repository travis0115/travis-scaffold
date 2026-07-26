export const messageTemplateParamTypeOptions = [
  { label: '文本', value: 'text' },
  { label: '数字', value: 'number' },
  { label: '金额', value: 'amount' },
  { label: '日期', value: 'date' },
  { label: '日期时间', value: 'datetime' },
  { label: '手机号', value: 'mobile' },
  { label: '邮箱', value: 'email' },
  { label: '链接', value: 'url' },
];

export const MESSAGE_TEMPLATE_PARAM_KEY_PATTERN = /^[A-Za-z][A-Za-z0-9_]*$/;
export const MESSAGE_TEMPLATE_PARAM_EXPRESSION_PATTERN =
  /\{\{\s*([^{}]+?)\s*}}/g;

export function isMessageTemplateParamType(type?: string) {
  return messageTemplateParamTypeOptions.some((item) => item.value === type);
}

const validators: Record<string, { message: string; pattern: RegExp }> = {
  amount: { message: '请输入合法金额，最多保留2位小数', pattern: /^-?\d+(\.\d{1,2})?$/ },
  date: { message: '请输入合法日期，格式为YYYY-MM-DD', pattern: /^\d{4}-\d{2}-\d{2}$/ },
  datetime: {
    message: '请输入合法日期时间，格式为YYYY-MM-DD HH:mm:ss',
    pattern: /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/,
  },
  email: { message: '请输入合法邮箱', pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
  mobile: { message: '请输入合法手机号', pattern: /^1[3-9]\d{9}$/ },
  number: { message: '请输入合法数字', pattern: /^-?\d+(\.\d+)?$/ },
  url: { message: '请输入以 http:// 或 https:// 开头的链接', pattern: /^https?:\/\/\S+$/ },
};
const dateValidator = validators.date!;
const datetimeValidator = validators.datetime!;

function isValidDate(value: string) {
  if (!dateValidator.pattern.test(value)) return false;
  const parts = value.split('-').map(Number);
  const [year, month, day] = parts as [number, number, number];
  const date = new Date(year, month - 1, day);
  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  );
}

function isValidDateTime(value: string) {
  if (!datetimeValidator.pattern.test(value)) return false;
  const [datePart, timePart] = value.split(' ');
  if (!datePart || !timePart) return false;
  return isValidDate(datePart) && /^([01]\d|2[0-3]):[0-5]\d:[0-5]\d$/.test(timePart);
}

export function getMessageTemplateParamTypeLabel(type?: string) {
  return messageTemplateParamTypeOptions.find((item) => item.value === type)?.label || type || '文本';
}

export function validateMessageTemplateParamValue(type: string, value: string) {
  if (!isMessageTemplateParamType(type)) return '参数类型不支持';
  if (!value) return true;
  if (type === 'text') return true;
  if (type === 'date') return isValidDate(value) || dateValidator.message;
  if (type === 'datetime') return isValidDateTime(value) || datetimeValidator.message;
  const validator = validators[type];
  if (!validator) return true;
  return validator.pattern.test(value) || validator.message;
}
