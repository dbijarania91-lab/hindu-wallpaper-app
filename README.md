# 🕉 Hindu Wallpaper App
**by Mahi Info** | Production-ready Android wallpaper app

---

## ✨ Features

| Feature | Details |
|---------|---------|
| 📸 Image Wallpapers | HD / FHD / 4K quality |
| 🎬 Live Wallpapers | Video-loop live wallpapers with direct apply |
| 📱 WhatsApp Status | Download & share directly to WA & Instagram Story |
| 🔥 Trending Section | Both image & live trending wallpapers |
| ⚡ Direct Apply | Set Home / Lock / Both screens instantly |
| 💾 Download | Save to gallery with progress notification |
| ❤️ Favorites | Offline-saved favorites |
| 🎨 iOS Glassmorphism UI | Industry-level dark saffron glass UI |
| 📣 AdMob Ads | Banner, Interstitial, Rewarded — IDs live from Supabase |

---

## 🏗 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM + Hilt DI
- **Database:** Supabase (PostgreSQL)
- **Storage:** Supabase Storage
- **Image Loading:** Coil
- **Video:** ExoPlayer / Media3
- **Ads:** Google AdMob
- **CI/CD:** GitHub Actions

---

## 🚀 Setup Guide

### Step 1 — Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/hindu-wallpaper-app.git
cd hindu-wallpaper-app
```

### Step 2 — Supabase Setup
1. Go to [supabase.com](https://supabase.com) → New project
2. Open **SQL Editor** → paste contents of `supabase_schema.sql` → Run
3. Go to **Storage** → create 3 buckets:
   - `wallpapers` (public)
   - `statuses` (public)
   - `thumbnails` (public)
4. Copy your **Project URL** and **anon/public key** from Settings → API

### Step 3 — AdMob Setup
1. Go to [admob.google.com](https://admob.google.com)
2. Create app → get App ID (starts with `ca-app-pub-XXXX~XXXX`)
3. Create 5 ad units: Banner, Interstitial, Rewarded, Native, App Open
4. In Supabase SQL Editor, run:
```sql
UPDATE app_config SET
  admob_banner_id       = 'ca-app-pub-YOURAPPID/UNIT1',
  admob_interstitial_id = 'ca-app-pub-YOURAPPID/UNIT2',
  admob_rewarded_id     = 'ca-app-pub-YOURAPPID/UNIT3',
  admob_native_id       = 'ca-app-pub-YOURAPPID/UNIT4',
  admob_open_app_id     = 'ca-app-pub-YOURAPPID/UNIT5',
  ads_enabled           = TRUE
WHERE id = 1;
```
5. In `app/build.gradle.kts`, replace AdMob App ID in `manifestPlaceholders`:
```kotlin
manifestPlaceholders["admobAppId"] = "ca-app-pub-YOURAPPID~APPID"
```

### Step 4 — Local properties
Create `local.properties` in root:
```
sdk.dir=/path/to/Android/sdk
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=your_anon_key_here
```

### Step 5 — Build & Run
```bash
./gradlew assembleDebug
```

---

## 🔑 GitHub Secrets (for CI/CD)

Add these in: **GitHub repo → Settings → Secrets → Actions**

| Secret Name | Value |
|-------------|-------|
| `SUPABASE_URL` | `https://xxxx.supabase.co` |
| `SUPABASE_ANON_KEY` | Your Supabase anon key |
| `KEYSTORE_BASE64` | `base64 -i keystore.jks` output |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | (Optional) for Play Store upload |

### Generate keystore:
```bash
keytool -genkey -v -keystore keystore.jks \
  -alias hinduwallpaper -keyalg RSA -keysize 2048 -validity 10000

# Encode to base64:
base64 -i keystore.jks | pbcopy   # macOS
base64 keystore.jks               # Linux
```

---

## 📦 CI/CD Workflow

| Trigger | Action |
|---------|--------|
| Push to `develop` | Build debug APK |
| Push to `main` | Build + sign release APK + AAB |
| Tag `v1.0.0` | Build + sign + create GitHub Release |
| Tag + `UPLOAD_TO_PLAY_STORE=true` | Auto-upload to Play Store internal track |

---

## 📁 Project Structure

```
app/src/main/java/com/mahiinfo/hinduwallpaper/
├── ads/              AdMobManager.kt — central ads controller
├── data/
│   ├── model/        Data models (Wallpaper, VideoStatus, AppConfig...)
│   ├── remote/       Supabase data source
│   ├── local/        Room DB (favorites, downloads)
│   └── repository/   WallpaperRepository
├── livewallpaper/    Live wallpaper WallpaperService
├── service/          DownloadService (foreground)
├── ui/
│   ├── screens/      HomeScreen, WallpaperList, Detail, Status, Favorites, More
│   ├── components/   GlassCard, WallpaperCard, StatusCard, SectionHeader...
│   └── theme/        Colors, Typography, HinduWallpaperTheme
├── viewmodel/        MainViewModel
├── MainActivity.kt   Navigation + BottomNav
└── HinduWallpaperApp.kt  Application + Hilt module
```

---

## 📲 Adding Content

Upload wallpapers in Supabase SQL Editor:
```sql
INSERT INTO wallpapers (title, title_hi, category, type, image_url, thumbnail_url, is_trending)
VALUES ('Lord Ganesha', 'श्री गणेश', 'Gods & Deities', 'image',
  'https://YOUR.supabase.co/storage/v1/object/public/wallpapers/ganesha.jpg',
  'https://YOUR.supabase.co/storage/v1/object/public/thumbnails/ganesha_t.jpg',
  TRUE);
```

---

## 📞 Contact
**Dev Choudhary** | Mahi Info  
📧 devendrabijarania41@gmail.com  
📱 Instagram: @devvv.20  
🎥 YouTube: @mahiinfo
