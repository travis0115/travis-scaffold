type TreeNodeId = number | string;

interface TreeNodeOptions {
  childrenField?: string;
  disabledField?: string;
  idField?: string;
}

type TreeNodeWithDisabled<T> = T & {
  children?: TreeNodeWithDisabled<T>[];
  disabled?: boolean;
};

function isSameTreeNodeId(a: unknown, b: unknown) {
  return `${a}` === `${b}`;
}

/**
 * 将指定节点及其全部子孙节点标记为禁用，适用于树选择器的上级节点选择场景。
 */
function disableTreeNodeAndDescendants<T extends Record<string, any>>(
  tree: T[],
  targetId?: null | TreeNodeId,
  options: TreeNodeOptions = {},
): Array<TreeNodeWithDisabled<T>> {
  if (targetId === undefined || targetId === null) {
    return tree;
  }

  const {
    childrenField = 'children',
    disabledField = 'disabled',
    idField = 'id',
  } = options;

  const disableNodes = (
    nodes: T[],
    ancestorDisabled = false,
  ): Array<TreeNodeWithDisabled<T>> =>
    nodes.map((node) => {
      const disabled =
        ancestorDisabled || isSameTreeNodeId(node[idField], targetId);
      const children = node[childrenField];

      return {
        ...node,
        [disabledField]: disabled,
        ...(Array.isArray(children) && children.length > 0
          ? {
              [childrenField]: disableNodes(children, disabled),
            }
          : {}),
      } as TreeNodeWithDisabled<T>;
    });

  return disableNodes(tree);
}

export { disableTreeNodeAndDescendants };
