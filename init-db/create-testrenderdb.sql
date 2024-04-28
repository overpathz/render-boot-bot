DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_database WHERE datname = 'testrenderdb') THEN
            CREATE DATABASE testrenderdb;
        END IF;
    END
$$;