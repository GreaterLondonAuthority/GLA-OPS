SELECT p.id prj_id, pdb.title project_title,
	year, month, ledger_status, ledger_type, spend_type, category, amount, ple.id ledger_id
FROM project_ledger_entry ple
LEFT JOIN project p ON ple.project_id = p.id
INNER JOIN v_project_block_active active_details_block ON active_details_block.project_id = p.id AND active_details_block.project_block_type = 'Details'
INNER JOIN project_details_block pdb ON active_details_block.id = pdb.id
WHERE (ledger_status = 'FORECAST' AND text(year_month) >= to_char(now(), 'YYYYMM'))
OR (ledger_status = 'ACTUAL' AND text(year_month) < to_char(now(), 'YYYYMM'))
OR (ledger_status = 'BUDGET')
ORDER BY prj_id, year DESC, month DESC
