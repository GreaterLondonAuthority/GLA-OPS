SELECT o.name org_name, u.username, r.name role_name, u.registered_on
FROM Users u
LEFT JOIN user_roles r ON u.username = r.username
LEFT JOIN organisation o ON r.organisation_id = o.id
WHERE (approved IS NULL OR NOT r.approved)
AND NOT (u.username = 'admin')
ORDER BY org_name, u.username