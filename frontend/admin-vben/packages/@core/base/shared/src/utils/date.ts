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

export const BACKEND_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

const ISO_OFFSET_DATETIME_REGEX =
  /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?(?:Z|[+-]\d{2}:?\d{2})$/;
const ISO_LOCAL_DATETIME_REGEX =
  /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?$/;
const BACKEND_DATETIME_REGEX = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
const BACKEND_DATETIME_GLOBAL_REGEX =
  /\b\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\b/g;

export function formatDate(time?: FormatDate, format: Format = 'YYYY-MM-DD') {
  if (time === undefined || time === null || time === '') {
    return '';
  }
  try {
    let date: dayjs.Dayjs;
    if (dayjs.isDayjs(time)) {
      date = time;
    } else if (
      typeof time === 'string' &&
      ISO_OFFSET_DATETIME_REGEX.test(time)
    ) {
      date = dayjs(time);
    } else if (
      typeof time === 'string' &&
      (BACKEND_DATETIME_REGEX.test(time) || ISO_LOCAL_DATETIME_REGEX.test(time))
    ) {
      // 后端返回的无时区时间字符串视为UTC时间
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
  return formatDate(time, BACKEND_DATETIME_FORMAT);
}

export function formatUtcDateTimesInText(value?: string) {
  if (!value) return value ?? '';
  return value.replaceAll(BACKEND_DATETIME_GLOBAL_REGEX, (dateTime) =>
    formatDateTime(dateTime),
  );
}

export function formatLocalDateToUtc(
  time?: FormatDate,
  format: Format = BACKEND_DATETIME_FORMAT,
) {
  if (time === undefined || time === null || time === '') {
    return '';
  }
  try {
    let date: dayjs.Dayjs;
    if (dayjs.isDayjs(time)) {
      date = time;
    } else if (
      typeof time === 'string' &&
      ISO_OFFSET_DATETIME_REGEX.test(time)
    ) {
      date = dayjs(time);
    } else if (
      typeof time === 'string' &&
      (BACKEND_DATETIME_REGEX.test(time) || ISO_LOCAL_DATETIME_REGEX.test(time))
    ) {
      date = dayjs.tz(time, currentTimezone);
    } else {
      date = dayjs(time);
    }
    if (!date.isValid()) {
      throw new Error('Invalid date');
    }
    return date.utc().format(format);
  } catch (error) {
    console.error(`Error formatting date to UTC: ${error}`);
    return String(time ?? '');
  }
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
