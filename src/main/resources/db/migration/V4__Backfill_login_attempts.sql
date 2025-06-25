INSERT INTO login_attempts (id, user_id, login_attempts, is_blocked, blocked_until, created_at, updated_at)
SELECT
    gen_random_uuid() AS id,
    u.id AS user_id,
    0 AS login_attempts,
    FALSE AS is_blocked,
    NULL AS blocked_until,
    CURRENT_TIMESTAMP AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM
    users u
WHERE
    NOT EXISTS (
        SELECT 1
        FROM login_attempts la
        WHERE la.user_id = u.id
    );
