select p.org_id as organisation_id, p.managing_organisation_id, p.programme_id, pg.name, o.team_id as team_id, psl.label_name as label from project p
inner join organisation o on p.org_id = o.id
inner join programme pg on p.programme_id = pg.id
inner join label l on p.id = l.project_id
inner join pre_set_label psl on l.pre_set_label_id = psl.id
where p.programme_id in (:programme_ids) and l.pre_set_label_id in (:label_ids)