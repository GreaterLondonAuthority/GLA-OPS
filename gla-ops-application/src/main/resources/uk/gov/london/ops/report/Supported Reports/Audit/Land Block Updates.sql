SELECT p.id prj_id, pdb.title, pb.*
FROM project_block pb
LEFT JOIN project p ON pb.project_id = p.id
INNER JOIN v_project_block_active active_details_block ON active_details_block.project_id = p.id AND active_details_block.project_block_type = 'Details'
INNER JOIN project_details_block pdb ON active_details_block.id = pdb.id
WHERE pb.last_modified IS NOT NULL
AND p.programme_id = 1001
ORDER BY pb.last_modified DESC