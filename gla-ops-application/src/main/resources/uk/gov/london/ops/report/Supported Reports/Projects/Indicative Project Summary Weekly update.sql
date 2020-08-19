

SELECT
tt.name tenure_type,
pdb.title project_title,
o.id org_id,
o.name organisation_name,
tpl.name template_name,
prg.name programme_name,
tu.project_id,
p.status,
itv.year,
itv.units,
ttt.tariff_cap tariff,
ttt.tariff_cap * itv.units total_grant
FROM indicative_tenure_value itv
LEFT JOIN tenure_and_units tu ON itv.tenure_units_id = tu.id
LEFT JOIN template_tenure_type ttt ON tu.tenure_type_id = ttt.id
LEFT JOIN tenure_type tt ON ttt.external_id = tt.id
LEFT JOIN project p ON tu.project_id = p.id
INNER JOIN v_project_block_active active_details_block ON active_details_block.project_id = p.id AND active_details_block.project_block_type = 'Details'
INNER JOIN project_details_block pdb ON active_details_block.id = pdb.id
LEFT JOIN organisation o ON p.org_id = o.id
LEFT JOIN template tpl ON tpl.id = tpl.id
LEFT JOIN programme prg ON prg.id = prg.id
