-- 1. enable pgcrypto for gen_random_bytes()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. link table
CREATE TABLE link (
    slug        text        PRIMARY KEY,
    url         text        NOT NULL UNIQUE,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- 3. slug generator (random base58)
CREATE OR REPLACE FUNCTION gen_random_slug(len int DEFAULT 5) RETURNS text AS $$
DECLARE
    alphabet text := '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
    slug text := '';
    bytea_bytes bytea := gen_random_bytes(len);
BEGIN
    FOR i IN 0..(len-1) LOOP
        slug := slug || substring(alphabet FROM get_byte(bytea_bytes, i) % 58 + 1 FOR 1);
    END LOOP;
    RETURN slug;
END;
$$ LANGUAGE plpgsql;

-- 4. safe slug generator
CREATE OR REPLACE FUNCTION gen_random_slug_trigger() RETURNS trigger AS $$
DECLARE
    max_attempts INT := 10;
    attempt INT := 0;
    new_slug TEXT;
BEGIN
    LOOP
        attempt := attempt + 1;
        new_slug := gen_random_slug();

        PERFORM 1 FROM link WHERE slug = new_slug;
        IF NOT FOUND THEN
            NEW.slug := new_slug;
            RETURN NEW;
        END IF;
        IF attempt >= max_attempts THEN
            RAISE EXCEPTION 'Failed to generate unique slug after % attempts', max_attempts;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 5. autogenerate slug during insert
CREATE TRIGGER set_random_slug
    BEFORE INSERT ON link
    FOR EACH ROW
    WHEN (NEW.slug IS NULL OR NEW.slug = '')
EXECUTE FUNCTION gen_random_slug_trigger();
