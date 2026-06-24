import type { SystemDictApi } from '#/api';

import { reactive } from 'vue';

import { getDictTree } from '#/api';

type DictOption = {
  color?: string;
  label: string;
  value: number | string;
};

const dictOptionsCache = new Map<string, DictOption[]>();
let dictTreeCache: SystemDictApi.SysDict[] | undefined;
let dictTreePromise: Promise<SystemDictApi.SysDict[]> | undefined;

function normalizeDictValue(value: string) {
  return /^-?\d+$/.test(value) ? Number(value) : value;
}

function normalizeTagColor(tagStyle?: string) {
  const colorMap: Record<string, string> = {
    danger: 'error',
    info: 'blue',
    primary: 'processing',
  };
  return tagStyle ? (colorMap[tagStyle] ?? tagStyle) : undefined;
}

function buildDictOptions(dict?: SystemDictApi.SysDict) {
  return (dict?.children ?? [])
    .filter((item) => item.status === 1)
    .map((item) => ({
      color: normalizeTagColor(item.tagStyle),
      label: item.label,
      value: normalizeDictValue(item.value),
    }));
}

function syncOptions(
  options: DictOption[],
  dicts: SystemDictApi.SysDict[],
  dictCode: string,
) {
  const dict = dicts.find((item) => item.dictCode === dictCode);
  options.splice(0, options.length, ...buildDictOptions(dict));
}

function syncCachedOptions(dicts: SystemDictApi.SysDict[]) {
  dictOptionsCache.forEach((options, dictCode) => {
    syncOptions(options, dicts, dictCode);
  });
}

function loadDictTree(force = false) {
  if (!force && dictTreePromise) return dictTreePromise;

  dictTreePromise = getDictTree()
    .then((dicts) => {
      dictTreeCache = dicts;
      syncCachedOptions(dicts);
      return dicts;
    })
    .catch((error) => {
      dictTreePromise = undefined;
      throw error;
    });
  return dictTreePromise;
}

export function getDictOptions(dictCode: string) {
  const cached = dictOptionsCache.get(dictCode);
  if (cached) return cached;

  const options = reactive<DictOption[]>([]);
  dictOptionsCache.set(dictCode, options);

  if (dictTreeCache) {
    syncOptions(options, dictTreeCache, dictCode);
  } else {
    loadDictTree();
  }

  return options;
}

export async function initDictOptions() {
  await loadDictTree();
}

export async function reloadDictOptions() {
  await loadDictTree(true);
}

export function getDictLabel(
  dictCode: string,
  value: number | string | undefined,
) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  const option = getDictOptions(dictCode).find((item) => item.value === value);
  return option?.label ?? value;
}
