ALTER TABLE VALUE_SECRET DROP CONSTRAINT FKOX93SY15F6PGBDR89KP05PNFQ;
ALTER TABLE PASSWORD_SECRET DROP CONSTRAINT FK31HQE03PKUGU8U5NG564KO2NV;
ALTER TABLE CERTIFICATE_SECRET DROP CONSTRAINT FK34BRQRQSRTKAF3GMTY1RJKYJD;
ALTER TABLE SSH_SECRET DROP CONSTRAINT SSH_SECRET_FKEY;
ALTER TABLE RSA_SECRET DROP CONSTRAINT RSA_SECRET_FKEY;

ALTER TABLE VALUE_SECRET DROP PRIMARY KEY;
ALTER TABLE PASSWORD_SECRET DROP PRIMARY KEY;
ALTER TABLE CERTIFICATE_SECRET DROP PRIMARY KEY;
ALTER TABLE SSH_SECRET DROP PRIMARY KEY;
ALTER TABLE RSA_SECRET DROP PRIMARY KEY;

ALTER TABLE VALUE_SECRET ADD PRIMARY KEY(UUID);
ALTER TABLE PASSWORD_SECRET ADD PRIMARY KEY(UUID);
ALTER TABLE CERTIFICATE_SECRET ADD PRIMARY KEY(UUID);
ALTER TABLE SSH_SECRET ADD PRIMARY KEY(UUID);
ALTER TABLE RSA_SECRET ADD PRIMARY KEY(UUID);

ALTER TABLE VALUE_SECRET DROP COLUMN IF EXISTS ID;
ALTER TABLE PASSWORD_SECRET DROP COLUMN IF EXISTS ID;
ALTER TABLE CERTIFICATE_SECRET DROP COLUMN IF EXISTS ID;
ALTER TABLE SSH_SECRET DROP COLUMN IF EXISTS ID;
ALTER TABLE RSA_SECRET DROP COLUMN IF EXISTS ID;

ALTER TABLE NAMED_SECRET DROP PRIMARY KEY;

ALTER TABLE NAMED_SECRET ADD PRIMARY KEY(UUID);

ALTER TABLE NAMED_SECRET DROP COLUMN IF EXISTS ID;