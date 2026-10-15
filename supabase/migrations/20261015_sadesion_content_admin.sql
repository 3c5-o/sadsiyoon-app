-- SADESION owner-only content catalogue. Apply in SADESION project SQL editor.
-- This file does NOT register owners: provision ONE verified existing auth user
-- privately after the migration (see docs/ADMIN_SETUP.md).
create extension if not exists pgcrypto;

create table if not exists public.sadesion_admins (
    user_id uuid primary key references auth.users(id) on delete cascade,
    role text not null check (role = 'owner'),
    active boolean not null default true,
    created_at timestamptz not null default now()
);

create or replace function public.sadesion_is_admin()
returns boolean language sql stable security definer set search_path = ''
as $$
    select exists (
      select 1
      from public.sadesion_admins a
      join auth.users u on u.id = a.user_id
      where a.user_id = auth.uid()
        and a.active and a.role = 'owner'
        and u.email_confirmed_at is not null
    );
$$;
revoke all on function public.sadesion_is_admin() from public;
grant execute on function public.sadesion_is_admin() to authenticated;

create table if not exists public.sadesion_content (
    id uuid primary key default gen_random_uuid(),
    kind text not null check (kind in
      ('cohort','subject','teacher','chapter','lesson','resource','exam','quiz','announcement')),
    parent_id uuid references public.sadesion_content(id) on delete set null,
    title text not null check (length(trim(title)) between 1 and 250),
    subtitle text not null default '',
    batch text not null default '',
    subject text not null default '',
    media_url text not null default '',
    media_type text not null default '',
    image_url text not null default '',
    source_url text not null default '',
    extra jsonb not null default '{}'::jsonb check (jsonb_typeof(extra)='object'),
    sort_index integer not null default 0,
    status text not null default 'draft' check (status in ('draft','published','hidden')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint sadesion_no_self_parent check (id is distinct from parent_id),
    constraint sadesion_https_urls check (
      (media_url='' or media_url ~ '^https://[^[:space:]]+$')
      and (image_url='' or image_url ~ '^https://[^[:space:]]+$')
      and (source_url='' or source_url ~ '^https://[^[:space:]]+$')
    )
);
create index if not exists sadesion_content_kind_sort
    on public.sadesion_content(kind,status,sort_index);
create index if not exists sadesion_content_parent_sort
    on public.sadesion_content(parent_id,sort_index);

create table if not exists public.sadesion_content_audit (
    id bigint generated always as identity primary key,
    content_id uuid,
    action text not null check (action in ('INSERT','UPDATE','DELETE')),
    actor_id uuid,
    old_record jsonb,
    new_record jsonb,
    created_at timestamptz not null default now()
);
create index if not exists sadesion_audit_by_date on
    public.sadesion_content_audit(created_at desc);

create or replace function public.sadesion_stamp_content()
returns trigger language plpgsql set search_path = ''
as $$
begin
    new.updated_at := now();
    return new;
end;
$$;
drop trigger if exists sadesion_stamp_content_trigger on public.sadesion_content;
create trigger sadesion_stamp_content_trigger before update on public.sadesion_content
for each row execute function public.sadesion_stamp_content();

create or replace function public.sadesion_log_content()
returns trigger language plpgsql security definer set search_path = ''
as $$
begin
    if TG_OP = 'INSERT' then
        insert into public.sadesion_content_audit(content_id,action,actor_id,new_record)
        values(new.id,TG_OP,auth.uid(),to_jsonb(new));
    elsif TG_OP = 'DELETE' then
        insert into public.sadesion_content_audit(content_id,action,actor_id,old_record)
        values(old.id,TG_OP,auth.uid(),to_jsonb(old));
    else
        insert into public.sadesion_content_audit(content_id,action,actor_id,old_record,new_record)
        values(new.id,TG_OP,auth.uid(),to_jsonb(old),to_jsonb(new));
    end if;
    return coalesce(new,old);
end;
$$;
drop trigger if exists sadesion_log_content_trigger on public.sadesion_content;
create trigger sadesion_log_content_trigger after insert or update or delete
on public.sadesion_content for each row execute function public.sadesion_log_content();

alter table public.sadesion_admins enable row level security;
alter table public.sadesion_content enable row level security;
alter table public.sadesion_content_audit enable row level security;
revoke all on public.sadesion_admins from anon,authenticated;
revoke all on public.sadesion_content_audit from anon,authenticated;
grant select on public.sadesion_content_audit to authenticated;
grant select on public.sadesion_content to anon;
grant select,insert,update,delete on public.sadesion_content to authenticated;

drop policy if exists sadesion_owner_see_audit on public.sadesion_content_audit;
create policy sadesion_owner_see_audit on public.sadesion_content_audit for select to authenticated
using (public.sadesion_is_admin());

drop policy if exists sadesion_public_read on public.sadesion_content;
create policy sadesion_public_read on public.sadesion_content for select to anon,authenticated
using (status='published' or (select public.sadesion_is_admin()));

drop policy if exists sadesion_owner_insert on public.sadesion_content;
create policy sadesion_owner_insert on public.sadesion_content for insert to authenticated
with check ((select public.sadesion_is_admin()));
drop policy if exists sadesion_owner_update on public.sadesion_content;
create policy sadesion_owner_update on public.sadesion_content for update to authenticated
using ((select public.sadesion_is_admin()))
with check ((select public.sadesion_is_admin()));
drop policy if exists sadesion_owner_delete on public.sadesion_content;
create policy sadesion_owner_delete on public.sadesion_content for delete to authenticated
using ((select public.sadesion_is_admin()));

revoke all on function public.sadesion_stamp_content() from public,anon,authenticated;
revoke all on function public.sadesion_log_content() from public,anon,authenticated;
