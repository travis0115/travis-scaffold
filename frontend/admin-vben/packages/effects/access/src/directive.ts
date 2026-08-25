/**
 * Global authority directive
 * Used for fine-grained control of component permissions
 * @Example v-access:role="[ROLE_NAME]" or v-access:role="ROLE_NAME"
 * @Example v-access:code="[ROLE_CODE]" or v-access:code="ROLE_CODE"
 */
import type { App, Directive, DirectiveBinding } from 'vue';

import { watchEffect } from 'vue';

import { useAccess } from './use-access';

const accessDirectiveStops = new WeakMap<Element, () => void>();
const originalDisplays = new WeakMap<HTMLElement, string>();

function isAccessible(binding: DirectiveBinding<string | string[]>) {
  const { accessMode, hasAccessByCodes, hasAccessByRoles } = useAccess();

  const value = binding.value;

  if (!value) return true;
  const authMethod =
    accessMode.value === 'frontend' && binding.arg === 'role'
      ? hasAccessByRoles
      : hasAccessByCodes;

  const values = Array.isArray(value) ? value : [value];

  return authMethod(values);
}

function setVisible(el: Element, visible: boolean) {
  const htmlEl = el as HTMLElement;
  if (!originalDisplays.has(htmlEl)) {
    originalDisplays.set(htmlEl, htmlEl.style.display);
  }
  htmlEl.style.display = visible
    ? (originalDisplays.get(htmlEl) ?? '')
    : 'none';
}

function bindAccessWatcher(
  el: Element,
  binding: DirectiveBinding<string | string[]>,
) {
  accessDirectiveStops.get(el)?.();
  const stop = watchEffect(() => {
    setVisible(el, isAccessible(binding));
  });
  accessDirectiveStops.set(el, stop);
}

const mounted = (el: Element, binding: DirectiveBinding<string | string[]>) => {
  bindAccessWatcher(el, binding);
};

const updated = (el: Element, binding: DirectiveBinding<string | string[]>) => {
  bindAccessWatcher(el, binding);
};

const beforeUnmount = (el: Element) => {
  accessDirectiveStops.get(el)?.();
  accessDirectiveStops.delete(el);
  originalDisplays.delete(el as HTMLElement);
};

const authDirective: Directive = {
  beforeUnmount,
  mounted,
  updated,
};

export function registerAccessDirective(app: App) {
  app.directive('access', authDirective);
}
