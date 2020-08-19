SELECT file_name, segment_number, interface_type, error_description, created_on, processed_on, content, id
FROM sap_data
WHERE NOT processed
ORDER BY created_on DESC