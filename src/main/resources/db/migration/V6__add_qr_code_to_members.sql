ALTER TABLE members ADD COLUMN qr_code VARCHAR(100);
ALTER TABLE members ADD CONSTRAINT uk_members_qr_code UNIQUE (qr_code);
