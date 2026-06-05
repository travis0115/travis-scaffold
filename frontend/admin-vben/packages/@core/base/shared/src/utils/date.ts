import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone.js';
import utc from 'dayjs/plugin/utc.js';

dayjs.extend(utc);
dayjs.extend(timezone);

type FormatDate = Date | dayjs.Dayjs | number | string;

type Format =
  | 'HH'
  | 'HH:mm'
  | 'HH:mm:ss'
  | 'YYYY'
  | 'YYYY-MM'
  | 'YYYY-MM-DD'
  | 'YYYY-MM-DD HH'
  | 'YYYY-MM-DD HH:mm'
  | 'YYYY-MM-DD HH:mm:ss'
  | (string & {});

/**
 * 判断时间字符串是否包含时区信息
 * 匹配：Z、z、+HH:mm、+HHmm、-HH:mm、-HHmm 等时区后缀
 */
const TIMEZONE_REGEX = /[Zz]|[+-]\d{2}:?\d{2}$/;

export function formatDate(time?: FormatDate, format: Format = 'YYYY-MM-DD') {
  if (time === undefined || time === null || time === '') {
    return '';
  }
  try {
    let date: dayjs.Dayjs;
    if (dayjs.isDayjs(time)) {
      date = time;
    } else if (typeof time === 'string' && !TIMEZONE_REGEX.test(time)) {
      // 无时区标记的字符串视为UTC时间（后端统一使用UTC）
      date = dayjs.utc(time);
    } else {
      date = dayjs(time);
    }
    if (!date.isValid()) {
      throw new Error('Invalid date');
    }
    return date.tz().format(format);
  } catch (error) {
    console.error(`Error formatting date: ${error}`);
    return String(time ?? '');
  }
}

export function formatDateTime(time?: FormatDate) {
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss');
}

export function isDate(value: any): value is Date {
  return value instanceof Date;
}

export function isDayjsObject(value: any): value is dayjs.Dayjs {
  return dayjs.isDayjs(value);
}

/**
 * 获取当前时区
 * @returns 当前时区
 */
export const getSystemTimezone = () => {
  return dayjs.tz.guess();
};

/**
 * 自定义设置的时区
 */
let currentTimezone = getSystemTimezone();

/**
 * 设置默认时区
 * @param timezone
 */
export const setCurrentTimezone = (timezone?: string) => {
  currentTimezone = timezone || getSystemTimezone();
  dayjs.tz.setDefault(currentTimezone);
};

/**
 * 获取设置的时区
 * @returns 设置的时区
 */
export const getCurrentTimezone = () => {
  return currentTimezone;
};
