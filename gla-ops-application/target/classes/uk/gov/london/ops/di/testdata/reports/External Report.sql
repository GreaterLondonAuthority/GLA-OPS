select p.org_id as organisation_id, p.managing_organisation_id, p.programme_id, pg.name, o.team_id as team_id from project p
inner join organisation o on p.org_id = o.id
inner join programme pg on p.programme_id = pg.id
where p.programme_id in (:programme_ids)
