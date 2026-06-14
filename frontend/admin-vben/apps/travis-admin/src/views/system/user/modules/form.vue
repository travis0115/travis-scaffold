<script lang="ts" setup>
import type { SystemUserApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import {
  assignUserRoles,
  createUser,
  getDeptTree,
  getUserDetail,
  updateUser,
} from '#/api';
import { isDeptEnabled } from '#/features';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemUserApi.SysUser>();
const deptTreeData = ref<any[]>([]);

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(deptTreeData.value),
  showDefaultActions: false,
});

const id = ref<number>();
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();

    try {
      // 分离角色ID，不在用户CRUD接口中传递
      const roleIds = values.roleIds;
      const userData = { ...values };
      delete (userData as any).roleIds;

      if (id.value) {
        // 编辑：更新用户信息 + 分配角色（不允许修改密码）
        delete (userData as any).password;
        await updateUser(id.value, userData);
        if (roleIds !== undefined) {
          await assignUserRoles({ userId: id.value, roleIds });
        }
      } else {
        // 新增：先创建用户获取ID，再分配角色
        const newUserId = await createUser(userData) as unknown as number;
        if (roleIds !== undefined && roleIds.length > 0) {
          await assignUserRoles({ userId: newUserId, roleIds });
        }
      }
      emits('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },

  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemUserApi.SysUser>();
      formApi.resetForm();

      if (data?.id) {
        formData.value = data;
        id.value = data.id;
      } else {
        id.value = undefined;
      }

      if (isDeptEnabled()) {
        // 预加载部门树数据，确保 TreeSelect 能正确显示名称
        try {
          deptTreeData.value = await getDeptTree();
          // 更新表单 schema 中的 deptTreeData
          formApi.updateSchema([
            {
              componentProps: {
                treeData: deptTreeData.value,
              },
              fieldName: 'deptId',
            },
          ]);
        } catch {
          deptTreeData.value = [];
        }
      } else {
        deptTreeData.value = [];
      }

      await nextTick();
      if (data?.id) {
        // 编辑时加载完整用户详情（含 roleIds）
        const detail = await getUserDetail(data.id);
        formApi.setValues(detail);
      }
    }
  },
});

const getDrawerTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.user.name')])
    : $t('ui.actionTitle.create', [$t('system.user.name')]);
});
</script>
<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
