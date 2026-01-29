-- Add user_id column to db_connections table
ALTER TABLE db_connections
    ADD COLUMN user_id BIGINT;

COMMENT ON COLUMN db_connections.user_id IS 'Associated system user ID, references sys_users.id';

-- Create index on user_id to improve query performance by user
CREATE INDEX IF NOT EXISTS idx_db_connections_user_id
    ON db_connections(user_id);

