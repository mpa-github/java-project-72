BEGIN;
ALTER TABLE url_check DROP CONSTRAINT fk_url_check_url_id;
TRUNCATE TABLE url_check RESTART IDENTITY;
TRUNCATE TABLE url RESTART IDENTITY;
ALTER TABLE url_check ADD CONSTRAINT fk_url_check_url_id FOREIGN KEY (url_id) REFERENCES url (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

-- TRUNCATE TABLE url RESTART IDENTITY CASCADE;

