-- CREATE TABLE (sesuai field di AuthAuditEntity)
create table if not exists audit_event (
                                           id          bigserial primary key,
                                           user_id     bigint null,
                                           event       varchar(64) not null,   -- contoh: AUTH_LOGIN_SUCCESS, AUTH_REFRESH_REUSE, dll.
    resource    varchar(255) null,      -- path endpoint: /api/auth/login
    method      varchar(16)  null,      -- GET/POST/PUT/DELETE
    user_agent  text null,
    ip          varchar(64)  null,
    reason      text null,              -- alasan singkat success/fail (tanpa data sensitif)
    status      integer not null default 0,  -- HTTP status
    created_at  timestamptz not null default now()
    );

-- INDEXES (umum untuk query audit)
create index if not exists idx_audit_event_user_created
    on audit_event (user_id, created_at desc);

create index if not exists idx_audit_event_event_created
    on audit_event (event, created_at desc);

create index if not exists idx_audit_event_status_created
    on audit_event (status, created_at desc);

-- (opsional) jika ingin cepat filter per endpoint:
-- create index if not exists idx_audit_event_resource_created
--     on audit_event (resource, created_at desc);