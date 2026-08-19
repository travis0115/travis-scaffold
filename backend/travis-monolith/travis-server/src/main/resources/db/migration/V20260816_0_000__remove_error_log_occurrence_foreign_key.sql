-- 错误日志发生明细改由业务代码维护关联删除，不依赖数据库外键级联。
SET @error_occurrence_fk_exists = (
  SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_error_log_occurrence'
    AND CONSTRAINT_NAME = 'fk_error_occurrence_log'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @drop_error_occurrence_fk_sql = IF(
  @error_occurrence_fk_exists > 0,
  'ALTER TABLE `sys_error_log_occurrence` DROP FOREIGN KEY `fk_error_occurrence_log`',
  'SELECT 1'
);

PREPARE drop_error_occurrence_fk_statement FROM @drop_error_occurrence_fk_sql;
EXECUTE drop_error_occurrence_fk_statement;
DEALLOCATE PREPARE drop_error_occurrence_fk_statement;
