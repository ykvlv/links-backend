CREATE TABLE IF NOT EXISTS enriched_click_event (
    timestamp           DateTime64(3, 'UTC'),
    event_date          Date,
    event_hour          UInt8,              -- eventHour (0-23)
    event_dow           UInt8,              -- eventDow (0-6)
    slug                String,
    resolved_url        String,
    ip                  IPv4,
    country_code        Nullable(String),
    country_name        Nullable(String),
    city_name           Nullable(String),
    asn                 Nullable(UInt32),
    asn_org             Nullable(String),
    browser             Nullable(String),
    os                  Nullable(String),
    device_type         Nullable(String),
    device_brand        Nullable(String),
    device_name         Nullable(String),
    accept_language     String,
    referer             String,
    referer_host        Nullable(String),
    referer_path        Nullable(String),
    origin              String,
    host                String,
    is_cache_hit        UInt8,              -- Boolean -> UInt8 (0/1)
    client_fingerprint  String,
    is_private_ip       UInt8               -- Boolean -> UInt8 (0/1)
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(event_date)
ORDER BY (slug, event_date)
SETTINGS index_granularity = 8192;
