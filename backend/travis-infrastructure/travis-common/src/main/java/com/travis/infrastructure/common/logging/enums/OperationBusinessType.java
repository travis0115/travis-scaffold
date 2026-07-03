package com.travis.infrastructure.common.logging.enums;

/** 操作日志业务类型。 */
public enum OperationBusinessType {
    /** 自动推断 */
    AUTO,

    /** 新增 */
    CREATE,

    /** 修改 */
    UPDATE,

    /** 删除 */
    DELETE,

    /** 授权 */
    GRANT,

    /** 上传 */
    UPLOAD,

    /** 导入 */
    IMPORT,

    /** 导出 */
    EXPORT,

    /** 其他 */
    OTHER
}
