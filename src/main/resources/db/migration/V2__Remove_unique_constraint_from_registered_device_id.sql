-- V2__Remove_unique_constraint_from_registered_device_id.sql

-- Kiểm tra và xóa constraint unique cho cột registered_device_id
DECLARE @ConstraintName NVARCHAR(200);

-- Tìm constraint unique cho cột registered_device_id
SELECT @ConstraintName = i.name
FROM sys.indexes i
         INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
         INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID('users')
  AND c.name = 'registered_device_id'
  AND i.is_unique = 1;

-- Nếu tìm thấy constraint, thực hiện xóa
IF @ConstraintName IS NOT NULL
BEGIN
EXEC('ALTER TABLE users DROP CONSTRAINT ' + @ConstraintName);
    PRINT 'Dropped unique constraint: ' + @ConstraintName;
END
ELSE
BEGIN
    PRINT 'No unique constraint found on registered_device_id column';
END;