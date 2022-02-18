select p.org_id as organisation_id, p.managing_organisation_id, p.programme_id, o.team_id as team_id from project p
inner join organisation o on p.org_id = o.id
where p.programme_id in (:programme_ids)

