create table if not exists auth_audit (
    id bigserial primary key,
    user_id bigint null,
    event varchar(32) not null, -- LOGIN_SUCCESS/LOGIN_FAIL/REFRESH_SUCCESS/REFRESH_REUSE/LOGOUT
    success boolean not null,
    reason text null,
    ip varchar(64) null,
    user_agent text null,
    created_at timestamptz not null default now()
    );

create index if not exists idx_auth_audit_user_created on auth_audit(user_id, created_at desc);
create index if not exists idx_auth_audit_event_created on auth_audit(event, created_at desc);