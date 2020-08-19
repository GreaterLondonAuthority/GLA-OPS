SELECT
	cols.table_name, cols.column_name, cols.data_type,
    (
        SELECT
            pg_catalog.col_description(c.oid, cols.ordinal_position::int)
        FROM
            pg_catalog.pg_class c
        WHERE
            c.oid = (SELECT ('"' || cols.table_name || '"')::regclass::oid)
            AND c.relname = cols.table_name
    ) AS column_comment
FROM
    information_schema.columns cols
WHERE
    cols.table_schema = 'public'
    AND NOT cols.table_name IN ('databasechangelog','databasechangeloglock','int_lock')
    AND NOT cols.table_name LIKE 'rs_%'
    AND NOT cols.table_name LIKE 'logging_%'
ORDER BY cols.table_name, cols.column_name
