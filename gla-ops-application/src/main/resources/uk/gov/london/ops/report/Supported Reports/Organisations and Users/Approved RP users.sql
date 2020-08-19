SELECT o.name org_name, u.username, r.name role_name, u.registered_on, u.firstname, u.lastname,
r.approved
FROM Users u
LEFT JOIN user_roles r ON u.username = r.username
LEFT JOIN organisation o ON r.organisation_id = o.id
WHERE r.approved
AND NOT (o.name = 'GLA')
ORDER BY org_name, u.username