SELECT Distinct v_project_details.project_id prj_id, v_project_details.* ,v_eligible_grant_1000.*,v_grant_source.*, v_milestones_1000.*,v_design_standards.*,v_questions_1000.*,v_add_questions_1000.*,v_indicative_1000.*,project.id, project.status

FROM (v_project_details LEFT JOIN v_eligible_grant_1000
ON v_project_details.project_id = v_eligible_grant_1000.project_id)
LEFT JOIN (
SELECT pb.project_id,gs.zero_grant_requested as gs_zero_grant_requested,gs.rcgf_value gs_rcgf_requested,gs.dpf_value gs_dpf_requested,gs.grant_value gs_grant_requested FROM grant_source_block gs INNER JOIN v_project_block_active pb ON gs.id = pb.id AND pb.block_type = 'GRANT_SOURCE'
) v_grant_source ON (v_project_details.project_id = v_grant_source.project_id)
LEFT JOIN v_milestones_1000 ON(v_project_details.project_id = v_milestones_1000.project_id)
LEFT JOIN v_design_standards ON(v_project_details.project_id = v_design_standards.project_id)
LEFT JOIN v_questions_1000 ON(v_project_details.project_id = v_questions_1000.project_id)
LEFT JOIN v_add_questions_1000 ON(v_project_details.project_id = v_add_questions_1000.project_id)
LEFT JOIN v_indicative_1000 ON(v_project_details.project_id = v_indicative_1000.project_id)
LEFT JOIN project ON(v_project_details.project_id = project.id)

WHERE project.status IN ('Assess','Returned')
ORDER BY project.id