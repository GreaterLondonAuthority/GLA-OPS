SELECT
	pb.project_id,
    MAX(pr.name) processing_route,
    MIN(CASE WHEN m.external_id = 3005 THEN milestone_date ELSE NULL END) land_aquired,
    MIN(CASE WHEN m.external_id = 3005 THEN monetary_split ELSE NULL END) land_aquired_split,
    MIN(CASE WHEN m.external_id = 3005 THEN milestone_status ELSE NULL END) land_aquired_status,
    MIN(CASE WHEN m.external_id = 3001 THEN milestone_date ELSE NULL END) contractor_appt,
    MIN(CASE WHEN m.external_id = 3001 THEN monetary_split ELSE NULL END) contractor_appt_split,
    MIN(CASE WHEN m.external_id = 3001 THEN milestone_status ELSE NULL END) contractor_appt_status,
    MIN(CASE WHEN m.external_id = 3006 THEN milestone_date ELSE NULL END) detailed_planning_permission_achieved,
    MIN(CASE WHEN m.external_id = 3006 THEN monetary_split ELSE NULL END) detailed_planning_permission_achieved_split,
    MIN(CASE WHEN m.external_id = 3006 THEN milestone_status ELSE NULL END) detailed_planning_permission_achieved_status,
    MIN(CASE WHEN m.external_id = 3003 THEN milestone_date ELSE NULL END) start_on_site,
    MIN(CASE WHEN m.external_id = 3003 THEN monetary_split ELSE NULL END) start_on_site_split,
    MIN(CASE WHEN m.external_id = 3003 THEN milestone_status ELSE NULL END) start_on_site_status,
    MIN(CASE WHEN m.external_id = 3004 THEN milestone_date ELSE NULL END) completion,
    MIN(CASE WHEN m.external_id = 3004 THEN monetary_split ELSE NULL END) completion_split,
    MIN(CASE WHEN m.external_id = 3004 THEN milestone_status ELSE NULL END) completion_status
FROM milestone m
LEFT JOIN project_block pb ON pb.id = m.milestones_block
LEFT JOIN milestones_block mb ON pb.id = mb.id
LEFT JOIN processing_route pr ON mb.processing_route_id = pr.id
GROUP BY pb.project_id
