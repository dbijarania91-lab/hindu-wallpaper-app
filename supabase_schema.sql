-- ============================================================
-- Hindu Wallpaper App — Supabase Database Schema
-- Run this in your Supabase SQL Editor (Dashboard → SQL Editor)
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── CATEGORIES ──────────────────────────────────────────────────────────────
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    name_hi     TEXT NOT NULL DEFAULT '',
    icon_url    TEXT DEFAULT '',
    banner_url  TEXT DEFAULT '',
    item_count  INT DEFAULT 0,
    type        TEXT NOT NULL DEFAULT 'wallpaper' CHECK (type IN ('wallpaper', 'status')),
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Seed categories
INSERT INTO categories (name, name_hi, type, sort_order) VALUES
  ('All',           'सभी',           'wallpaper', 0),
  ('Gods & Deities','देवी देवता',    'wallpaper', 1),
  ('Temples',       'मंदिर',         'wallpaper', 2),
  ('Festivals',     'त्यौहार',       'wallpaper', 3),
  ('Mahadev',       'महादेव',        'wallpaper', 4),
  ('Ram Mandir',    'राम मंदिर',     'wallpaper', 5),
  ('Durga Mata',    'दुर्गा माता',   'wallpaper', 6),
  ('Krishna',       'कृष्ण',         'wallpaper', 7),
  ('Hanuman',       'हनुमान',        'wallpaper', 8),
  ('Nature',        'प्रकृति',       'wallpaper', 9),
  ('Navratri',      'नवरात्रि',      'status',    1),
  ('Diwali',        'दिवाली',        'status',    2),
  ('Good Morning',  'सुप्रभात',      'status',    3),
  ('Bhajan',        'भजन',           'status',    4),
  ('Motivational',  'प्रेरणादायक',  'status',    5);

-- ─── WALLPAPERS ──────────────────────────────────────────────────────────────
CREATE TABLE wallpapers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    title_hi        TEXT DEFAULT '',
    category        TEXT NOT NULL,
    type            TEXT NOT NULL DEFAULT 'image' CHECK (type IN ('image', 'live', 'gif')),
    image_url       TEXT NOT NULL,
    thumbnail_url   TEXT NOT NULL,
    live_url        TEXT,             -- For live wallpapers (video loop URL)
    resolution      TEXT DEFAULT 'FHD',
    tags            TEXT[] DEFAULT '{}',
    download_count  INT DEFAULT 0,
    view_count      INT DEFAULT 0,
    like_count      INT DEFAULT 0,
    is_trending     BOOLEAN DEFAULT FALSE,
    is_premium      BOOLEAN DEFAULT FALSE,
    storage_path    TEXT DEFAULT '',
    file_size_kb    BIGINT DEFAULT 0,
    color_palette   TEXT[] DEFAULT '{}',
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for fast queries
CREATE INDEX idx_wallpapers_category ON wallpapers(category);
CREATE INDEX idx_wallpapers_type ON wallpapers(type);
CREATE INDEX idx_wallpapers_trending ON wallpapers(is_trending) WHERE is_trending = TRUE;
CREATE INDEX idx_wallpapers_created ON wallpapers(created_at DESC);
CREATE INDEX idx_wallpapers_tags ON wallpapers USING gin(tags);

-- ─── VIDEO STATUSES ───────────────────────────────────────────────────────────
CREATE TABLE video_statuses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    title_hi        TEXT DEFAULT '',
    category        TEXT NOT NULL,
    video_url       TEXT NOT NULL,
    thumbnail_url   TEXT NOT NULL,
    duration_sec    INT DEFAULT 30,
    resolution      TEXT DEFAULT '1080p',
    download_count  INT DEFAULT 0,
    is_trending     BOOLEAN DEFAULT FALSE,
    file_size_kb    BIGINT DEFAULT 0,
    tags            TEXT[] DEFAULT '{}',
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_statuses_category ON video_statuses(category);
CREATE INDEX idx_statuses_trending ON video_statuses(is_trending) WHERE is_trending = TRUE;
CREATE INDEX idx_statuses_created ON video_statuses(created_at DESC);

-- ─── APP CONFIG (AdMob IDs + feature flags) ──────────────────────────────────
-- ⚠️  THIS IS THE TABLE YOU EDIT TO ADD YOUR ADMOB IDs
CREATE TABLE app_config (
    id                      INT PRIMARY KEY DEFAULT 1,

    -- 🔑 AdMob Ad Unit IDs — paste your real IDs here from AdMob dashboard
    admob_banner_id         TEXT DEFAULT 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX',
    admob_interstitial_id   TEXT DEFAULT 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX',
    admob_rewarded_id       TEXT DEFAULT 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX',
    admob_native_id         TEXT DEFAULT 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX',
    admob_open_app_id       TEXT DEFAULT 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX',

    -- Ads settings
    ads_enabled             BOOLEAN DEFAULT TRUE,
    interstitial_interval   INT DEFAULT 3,      -- Show after every N navigations
    rewarded_for_premium    BOOLEAN DEFAULT FALSE,

    -- App update config
    app_version             TEXT DEFAULT '1.0.0',
    force_update            BOOLEAN DEFAULT FALSE,
    min_required_version    TEXT DEFAULT '1.0.0',
    play_store_url          TEXT DEFAULT '',
    maintenance_mode        BOOLEAN DEFAULT FALSE,
    maintenance_message     TEXT DEFAULT 'App is under maintenance. Please try later.',

    -- Links
    privacy_policy_url      TEXT DEFAULT '',
    terms_url               TEXT DEFAULT '',
    rate_us_url             TEXT DEFAULT '',
    whatsapp_channel        TEXT DEFAULT '',
    telegram_channel        TEXT DEFAULT '',
    youtube_channel         TEXT DEFAULT 'https://youtube.com/@mahiinfo',
    instagram_url           TEXT DEFAULT '',

    updated_at              TIMESTAMPTZ DEFAULT NOW(),

    CONSTRAINT single_row CHECK (id = 1)
);

-- Insert default config row
INSERT INTO app_config (id) VALUES (1)
ON CONFLICT (id) DO NOTHING;

-- ─── ANALYTICS / DOWNLOAD TRACKING ───────────────────────────────────────────
CREATE TABLE download_events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id      UUID NOT NULL,
    content_type    TEXT NOT NULL CHECK (content_type IN ('wallpaper', 'status')),
    event_type      TEXT NOT NULL CHECK (event_type IN ('download', 'view', 'share', 'apply')),
    device_id       TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_content ON download_events(content_id, content_type);

-- ─── RPC: Increment download count ────────────────────────────────────────────
CREATE OR REPLACE FUNCTION increment_download(wallpaper_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE wallpapers
    SET download_count = download_count + 1,
        updated_at = NOW()
    WHERE id = wallpaper_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION increment_status_download(status_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE video_statuses
    SET download_count = download_count + 1
    WHERE id = status_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Auto-update category item_count
CREATE OR REPLACE FUNCTION update_category_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE categories
    SET item_count = (
        SELECT COUNT(*) FROM wallpapers WHERE category = NEW.category
    )
    WHERE name = NEW.category AND type = 'wallpaper';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER wallpaper_count_trigger
AFTER INSERT OR DELETE ON wallpapers
FOR EACH ROW EXECUTE FUNCTION update_category_count();

-- ─── ROW LEVEL SECURITY ───────────────────────────────────────────────────────
-- Public read for all content
ALTER TABLE wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE video_statuses ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public read wallpapers"    ON wallpapers       FOR SELECT USING (TRUE);
CREATE POLICY "Public read statuses"      ON video_statuses   FOR SELECT USING (TRUE);
CREATE POLICY "Public read categories"    ON categories       FOR SELECT USING (TRUE);
CREATE POLICY "Public read app_config"    ON app_config       FOR SELECT USING (TRUE);

-- Only authenticated (service role) can insert/update
CREATE POLICY "Service write wallpapers"  ON wallpapers       FOR ALL USING (auth.role() = 'service_role');
CREATE POLICY "Service write statuses"    ON video_statuses   FOR ALL USING (auth.role() = 'service_role');
CREATE POLICY "Service write categories"  ON categories       FOR ALL USING (auth.role() = 'service_role');
CREATE POLICY "Service write app_config"  ON app_config       FOR ALL USING (auth.role() = 'service_role');

-- ─── STORAGE BUCKETS ─────────────────────────────────────────────────────────
-- Run in Supabase Dashboard → Storage → New Bucket
-- Bucket name: wallpapers  (public: TRUE)
-- Bucket name: statuses    (public: TRUE)
-- Bucket name: thumbnails  (public: TRUE)
-- File size limit: 50MB for videos, 10MB for images

-- ─── SAMPLE DATA ─────────────────────────────────────────────────────────────
-- Add sample wallpapers (replace URLs with your Supabase storage URLs)
INSERT INTO wallpapers (title, title_hi, category, type, image_url, thumbnail_url, is_trending, tags)
VALUES
  ('Lord Shiva 4K', 'भगवान शिव', 'Mahadev', 'image',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/wallpapers/shiva_4k.jpg',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/thumbnails/shiva_thumb.jpg',
   TRUE, ARRAY['shiva', 'mahadev', 'om namah shivay']),

  ('Jai Shree Ram', 'जय श्री राम', 'Ram Mandir', 'image',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/wallpapers/ram_mandir.jpg',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/thumbnails/ram_thumb.jpg',
   TRUE, ARRAY['ram', 'ram mandir', 'ayodhya']),

  ('Durga Mata', 'माँ दुर्गा', 'Durga Mata', 'live',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/wallpapers/durga_mata.jpg',
   'https://YOUR_PROJECT.supabase.co/storage/v1/object/public/thumbnails/durga_thumb.jpg',
   TRUE, ARRAY['durga', 'navratri', 'mata']);

-- ============================================================
-- HOW TO UPDATE ADMOB IDs (no app update needed!):
-- Just run this SQL in Supabase SQL Editor:
--
-- UPDATE app_config SET
--   admob_banner_id       = 'ca-app-pub-YOUR_ID/YOUR_UNIT',
--   admob_interstitial_id = 'ca-app-pub-YOUR_ID/YOUR_UNIT',
--   admob_rewarded_id     = 'ca-app-pub-YOUR_ID/YOUR_UNIT',
--   admob_native_id       = 'ca-app-pub-YOUR_ID/YOUR_UNIT',
--   admob_open_app_id     = 'ca-app-pub-YOUR_ID/YOUR_UNIT',
--   ads_enabled           = TRUE,
--   interstitial_interval = 3,
--   updated_at            = NOW()
-- WHERE id = 1;
-- ============================================================
