SELECT  r.*, u.lastname, u.firstname, u.last_logged_on


FROM user_roles r

LEFT JOIN users u ON u.username = r.username
WHERE r.organisation_id = 10000
AND approved
ORDER BY last_logged_on